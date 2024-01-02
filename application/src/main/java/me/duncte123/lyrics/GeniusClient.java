package me.duncte123.lyrics;

import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.duncte123.lyrics.exception.LyricsNotFoundException;
import me.duncte123.lyrics.model.AlbumArt;
import me.duncte123.lyrics.model.Lyrics;
import me.duncte123.lyrics.model.TextLyrics;
import me.duncte123.lyrics.model.Track;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GeniusClient implements AutoCloseable {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final HttpInterfaceManager httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();
    private final String apiKey;

    public GeniusClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public Future<Lyrics> findLyrics(AudioTrack track) {
        return search(
                "%s - %s".formatted(track.getInfo().title, track.getInfo().author)
        );
    }

    public Future<Lyrics> search(String query) {
        return executor.submit(() -> {
            try {
                final var geniusData = findGeniusData(query);
                final var lyricsText = loadLyrics(geniusData.url());

                return new TextLyrics(
                        new Track(
                                geniusData.title(),
                                geniusData.author(),
                                null,
                                List.of(
                                        new AlbumArt(
                                                geniusData.artwork(),
                                                -1, -1
                                        )
                                )
                        ),
                        "genius.com",
                        lyricsText
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public HttpInterface getHttpInterface() {
        return this.httpInterfaceManager.getInterface();
    }

    private GeniusData findGeniusData(String query) throws IOException {
        final var request = new HttpGet("https://api.genius.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8));

        request.setHeader("Authorization", "Bearer " + this.apiKey);

        try (final var response = getHttpInterface().execute(request)) {
            final var browser = JsonBrowser.parse(response.getEntity().getContent());
            final long httpStatus = browser.get("meta").get("status").asLong(-1L);

            if (httpStatus != 200L) {
                throw new LyricsNotFoundException();
            }

            final var hits = browser.get("response").get("hits");

            // wat??
            if (!hits.isList()) {
                throw new LyricsNotFoundException();
            }

            final var hitValues = hits.values();

            if (hitValues.isEmpty()) {
                throw new LyricsNotFoundException();
            }

            final var firstHit = hitValues.stream()
                    .filter((it) -> "song".equals(it.get("type").text()))
                    .findFirst()
                    .orElseThrow(LyricsNotFoundException::new);

            final var result = firstHit.get("result");

            return new GeniusData(
                    result.get("url").text(),
                    result.get("title").text(),
                    result.get("artist_names").text(),
                    result.get("song_art_image_url").text()
            );
        }
    }

    private String loadLyrics(String geniusUrl) throws IOException {
        final var request = new HttpGet(geniusUrl);

        try (final var response = getHttpInterface().execute(request)) {
            final String html = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            final var doc = Jsoup.parse(html);

            final var lyricsContainer = doc.select("div[data-lyrics-container]").first();

            if (lyricsContainer == null) {
                throw new RuntimeException("Could not find lyrics container, please report this to the developer");
            }

            return lyricsContainer.wholeText()
                    .replace("<br><br>", "\n")
                    .replace("<br>", "\n")
                    .replace("\n\n\n", "\n")
                    .trim();
        }
    }

    @Override
    public void close() {
        ExceptionTools.closeWithWarnings(this.httpInterfaceManager);
        executor.shutdown();
    }

    private record GeniusData(
            String url,
            String title,
            String author,
            String artwork
    ) {
    }
}
