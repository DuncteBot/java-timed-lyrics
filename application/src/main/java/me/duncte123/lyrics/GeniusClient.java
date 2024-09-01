package me.duncte123.lyrics;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
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
    private final HttpClientProvider httpInterfaceManager;
    private final String apiKey;
    private static final String BROWSER_USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:129.0) Gecko/20100101 Firefox/129.0";
    private static final String PRELOAD_START = "window.__PRELOADED_STATE__ = JSON.parse('";
    private static final String PRELOAD_END = "');";

    public GeniusClient(String apiKey, HttpClientProvider httpProvider) {
        this.apiKey = apiKey;
        this.httpInterfaceManager = httpProvider;
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
        return this.httpInterfaceManager.getHttpInterface();
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

        request.setHeader("user-agent", BROWSER_USER_AGENT);

        try (final var response = getHttpInterface().execute(request)) {
            final String html = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

            // fucking kill me
            final var idx1 = html.indexOf(PRELOAD_START);
            final var split1 = html.substring(idx1 + PRELOAD_START.length());
            final var idx2 = split1.indexOf(PRELOAD_END);
            final var json = split1.substring(0, idx2)
                    .replace("\\\"", "\"")
                    .replace("\\'", "'")
                    .replace("\\\\", "\\");

            System.out.println(json);

            final var lyrics = JsonBrowser.parse(json).get("songPage").get("lyricsData").get("body").text();

            if (lyrics == null || lyrics.isEmpty()) {
                final var doc = Jsoup.parse(html);

                final var lyricsContainer = doc.select("[data-lyrics-container]").first();

                if (lyricsContainer == null) {
                    throw new RuntimeException("Could not find lyrics container, please report this to the developer");
                }

                return lyricsContainer.wholeText()
                        .replace("<br><br>", "\n")
                        .replace("<br>", "\n")
                        .replace("\n\n\n", "\n")
                        .trim();
            }

            return lyrics
                    .replace("<br><br>", "\n")
                    .replace("<br>", "\n")
                    .replace("\n\n\n", "\n")
                    .trim();
        }
    }

    @Override
    public void close() {
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
