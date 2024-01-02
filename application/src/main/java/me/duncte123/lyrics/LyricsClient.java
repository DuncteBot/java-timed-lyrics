package me.duncte123.lyrics;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.duncte123.lyrics.exception.LyricsNotFoundException;
import me.duncte123.lyrics.model.*;
import me.duncte123.lyrics.model.request.BrowseRequest;
import me.duncte123.lyrics.model.request.NextRequest;
import me.duncte123.lyrics.model.request.SearchRequest;
import me.duncte123.lyrics.utils.JsonUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static me.duncte123.lyrics.utils.JsonUtils.getRunningText;
import static me.duncte123.lyrics.utils.YouTubeUtils.*;

public class LyricsClient implements AutoCloseable {
    private static final String API_URL = "https://music.youtube.com/youtubei/v1";
    private static final String BROWSE_URL = API_URL + "/browse";
    private static final String NEXT_URL = API_URL + "/next";
    private static final String SEARCH_URL = API_URL + "/search";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final HttpInterfaceManager httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();

    public HttpInterface getHttpInterface() {
        return this.httpInterfaceManager.getInterface();
    }

    @Override
    public void close() throws Exception {
        ExceptionTools.closeWithWarnings(this.httpInterfaceManager);
        executor.shutdown();
    }

    public Future<Lyrics> findLyrics(AudioTrack track) {
        try {
            final String videoId;

            if (track instanceof YoutubeAudioTrack ytTrack) {
                videoId = ytTrack.getInfo().identifier;
            } else if (track.getInfo().isrc != null) {
                // So, turns out that yt needs the ISRC in quotes. Whoops
                final var searched = search(
                        '"' + track.getInfo().isrc + '"'
                ).get();

                if (searched.isEmpty()) {
                    throw new LyricsNotFoundException();
                }

                videoId = searched.get(0).videoId();
            } else {
                final var searched = search(
                        "%s - %s".formatted(track.getInfo().title, track.getInfo().author)
                ).get();

                if (searched.isEmpty()) {
                    throw new LyricsNotFoundException();
                }

                videoId = searched.get(0).videoId();
            }

            return requestLyrics(videoId);
        } catch (InterruptedException | ExecutionException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public Future<Lyrics> requestLyrics(String videoId) {
        return executor.submit(() -> {
            final var nextPage = requestNextPage(videoId);
            final var browseId = getBrowseEndpoint(nextPage);

            if (browseId == null) throw new LyricsNotFoundException();

            final var browseResult = request(BROWSE_URL, new BrowseRequest(Context.DEFAULT_MOBILE_REQUEST, browseId));

            final var lyricsData = getLyricsData(browseResult);
            final var albumArt = getThumbnails(nextPage);

            if (lyricsData.isNull()) {
                final var renderer = getMusicDescriptionShelfRenderer(browseResult);

                if (renderer.isNull()) throw new LyricsNotFoundException();

                final var text = getRunningText(renderer, "description");
                final var source = getRunningText(renderer, "footer");

                return new TextLyrics(getTrack(nextPage, albumArt), source, text);
            }

            final var source = getSource(lyricsData);
            final var lines = getLines(lyricsData);

            return new TimedLyrics(getTrack(nextPage, albumArt), source, lines);
        });
    }

    public Future<List<SearchTrack>> search(String query) {
        return search(query, null);
    }

    public Future<List<SearchTrack>> search(String query, String region) {
        return executor.submit(() -> {
            final var resList = new ArrayList<SearchTrack>();

            final var result = request(SEARCH_URL, new SearchRequest(
                    Context.DEFAULT_MOBILE_REQUEST_WITH_REGION.apply(region),
                    query
            ));

            // /contents/tabbedSearchResultsRenderer/tabs/0/tabRenderer/content/sectionListRenderer/contents/1/musicCardShelfRenderer/title/runs/0/navigationEndpoint/watchEndpoint/videoId
            final var contents = result
                    .get("contents")
                    .get("tabbedSearchResultsRenderer")
                    .get("tabs")
                    .index(0)
                    .get("tabRenderer")
                    .get("content")
                    .get("sectionListRenderer")
                    .get("contents");

            contents.values()
                    .stream()
                    .filter((it) -> !it.get("musicCardShelfRenderer").isNull())
                    .findFirst()
                    .ifPresent((it) -> {
                        final var renderer = it.get("musicCardShelfRenderer");

                        final var title = getRunningText(renderer, "title");
                        final var videoId = renderer.get("buttons")
                                .index(0)
                                .get("buttonRenderer")
                                .get("command")
                                .get("watchEndpoint")
                                .get("videoId")
                                .text();

                        if (title != null && videoId != null) {
                            resList.add(new SearchTrack(videoId, title));
                        }
                    });

            contents.values()
                    .stream()
                    .filter(
                            (it) -> it.get("musicShelfRenderer")
                                    .get("contents")
                                    .values()
                                    .stream()
                                    .anyMatch(
                                            (content) -> content.get("musicTwoColumnItemRenderer")
                                                    .get("navigationEndpoint")
                                                    .get("watchEndpoint")
                                                    .get("videoId")
                                                    .text() != null
                                    )
                    )
                    .findFirst()
                    .ifPresent((it) ->
                            it.get("musicShelfRenderer")
                                    .get("contents")
                                    .values()
                                    .forEach((item) -> {
                                        final var renderer = item.get("musicTwoColumnItemRenderer");

                                        final var title = getRunningText(renderer, "title");
                                        final var videoId = renderer.get("navigationEndpoint")
                                                .get("watchEndpoint")
                                                .get("videoId")
                                                .text();

                                        if (title != null && videoId != null) {
                                            resList.add(new SearchTrack(videoId, title));
                                        }
                                    })
                    );

            return resList;
        });
    }

    private JsonBrowser request(String url, Object body) throws IOException {
        final var request = new HttpPost(url);

        request.setHeader("Content-Type", "application/json");

        final var encodedBody = JsonUtils.toJsonString(body);

        request.setEntity(new StringEntity(encodedBody, "UTF-8"));

        try (final var response = getHttpInterface().execute(request)) {
            return JsonBrowser.parse(response.getEntity().getContent());
        }
    }

    private JsonBrowser requestNextPage(String videoId) throws IOException {
        return request(NEXT_URL, new NextRequest(Context.DEFAULT_MOBILE_REQUEST, videoId));
    }
}
