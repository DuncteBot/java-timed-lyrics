package me.duncte123.lyrics.utils;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import jakarta.annotation.Nullable;
import me.duncte123.lyrics.exception.LyricsNotFoundException;
import me.duncte123.lyrics.model.AlbumArt;
import me.duncte123.lyrics.model.Line;
import me.duncte123.lyrics.model.LongRange;
import me.duncte123.lyrics.model.Track;

import java.util.List;

import static me.duncte123.lyrics.utils.JsonUtils.getRunningText;

public final class YouTubeUtils {
    @Nullable
    public static String getBrowseEndpoint(JsonBrowser browser) {
        return browser.get("contents")
                .get("singleColumnMusicWatchNextResultsRenderer")
                .get("tabbedRenderer")
                .get("watchNextTabbedResultsRenderer")
                .get("tabs")
                .index(1)
                .get("tabRenderer")
                .get("endpoint")
                .get("browseEndpoint")
                .get("browseId")
                .text();
    }

    public static JsonBrowser getLyricsData(JsonBrowser browser) {
        return browser.get("contents")
                .get("elementRenderer")
                .get("newElement")
                .get("type")
                .get("componentType")
                .get("model")
                .get("timedLyricsModel")
                .get("lyricsData");
    }

    public static String getSource(JsonBrowser browser) {
        final var sourceMessage = browser.get("sourceMessage").text();

        if (sourceMessage == null) {
            throw new LyricsNotFoundException();
        }

        final var semiIndex = sourceMessage.indexOf(": ");

        return sourceMessage.substring(semiIndex + 2);
    }

    public static Track getTrack(JsonBrowser browser, List<AlbumArt> albumArt) {
        final var lockScreen = browser.get("lockscreen").get("lockScreenRenderer");

        if (lockScreen.isNull()) {
            throw new LyricsNotFoundException();
        }

        final var title = getRunningText(lockScreen, "title");
        final var author = getRunningText(lockScreen, "shortBylineText");
        final var album = getRunningText(lockScreen, "albumText");

        if (title == null || author == null || album == null) {
            throw new LyricsNotFoundException();
        }

        return new Track(title, author, album, albumArt);
    }

    public static JsonBrowser getMusicDescriptionShelfRenderer(JsonBrowser browser) {
        return browser.get("contents")
                .get("sectionListRenderer")
                .get("contents")
                .index(0)
                .get("musicDescriptionShelfRenderer");
    }

    public static List<AlbumArt> getThumbnails(JsonBrowser browser) {
        final var thumbnails = browser.get("contents")
                .get("singleColumnMusicWatchNextResultsRenderer")
                .get("tabbedRenderer")
                .get("watchNextTabbedResultsRenderer")
                .get("tabs")
                .index(0)
                .get("tabRenderer")
                .get("content")
                .get("musicQueueRenderer")
                .get("content")
                .get("playlistPanelRenderer")
                .get("contents")
                .index(0)
                .get("playlistPanelVideoRenderer")
                .get("thumbnail")
                .get("thumbnails");

        if (thumbnails.isList()) {
            return thumbnails.values()
                    .stream()
                    .map((it) -> it.as(AlbumArt.class))
                    .toList();
        } else {
            return List.of();
        }
    }

    public static List<Line> getLines(JsonBrowser browser) {
        final var linesData = browser.get("timedLyricsData");

        if (linesData.isNull() || !linesData.isList()) {
            throw new LyricsNotFoundException();
        }

        return linesData.values()
                .stream()
                .map((it) -> {
                    final var line = it.get("lyricLine").text();

                    final var cueRange = it.get("cueRange");
                    final var start = cueRange.get("startTimeMilliseconds");
                    final var end = cueRange.get("endTimeMilliseconds");

                    if (start.isNull() || end.isNull()) {
                        throw new RuntimeException("Could not calculate range");
                    }

                    final var range = new LongRange(start.asLong(-1), end.asLong(-1));

                    return new Line(line, range);
                })
                .toList();
    }
}
