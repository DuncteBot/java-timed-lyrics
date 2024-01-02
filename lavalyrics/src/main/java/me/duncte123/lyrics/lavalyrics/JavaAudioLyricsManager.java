package me.duncte123.lyrics.lavalyrics;

import com.github.topi314.lavalyrics.AudioLyricsManager;
import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.github.topi314.lavalyrics.lyrics.BasicAudioLyrics;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.duncte123.lyrics.GeniusClient;
import me.duncte123.lyrics.LyricsClient;
import me.duncte123.lyrics.exception.LyricsNotFoundException;
import me.duncte123.lyrics.lavalink.Config;
import me.duncte123.lyrics.model.Lyrics;
import me.duncte123.lyrics.model.TextLyrics;
import me.duncte123.lyrics.model.TimedLyrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavaAudioLyricsManager implements AudioLyricsManager {
    private final LyricsClient youtubeClient = new LyricsClient();
    private final GeniusClient geniusClient;

    public JavaAudioLyricsManager(Config config) {
        final String geniusApiKey = config.getGeniusApiKey();

        if (geniusApiKey == null || geniusApiKey.isBlank()) {
            geniusClient = null;
        } else {
            geniusClient = new GeniusClient(geniusApiKey);
        }
    }

    @Override
    public @NotNull String getSourceName() {
        return "java-timed-lyrics-plugin";
    }

    private Lyrics attemptYoutubeLoad(AudioTrack track) {
        try {
            return this.youtubeClient.findLyrics(track).get();
        } catch (Exception e) {
            if (e.getCause() instanceof LyricsNotFoundException) {
                return null;
            }

            throw new FriendlyException("Failure when searching for youtube lyrics", Severity.COMMON, e);
        }
    }

    private Lyrics attemptGeniusLoad(AudioTrack track) {
        try {
            return this.geniusClient.findLyrics(track).get();
        } catch (Exception e) {
            if (e.getCause() instanceof LyricsNotFoundException) {
                return null;
            }

            throw new FriendlyException("Failure when searching for genius lyrics", Severity.COMMON, e);
        }
    }

    @Override
    public @Nullable AudioLyrics loadLyrics(@NotNull AudioTrack audioTrack) {
        Lyrics lyrics = attemptYoutubeLoad(audioTrack);

        if (lyrics == null) {
            lyrics = attemptGeniusLoad(audioTrack);
        }

        return Optional.ofNullable(lyrics)
                .map((it) -> {
                    if (it instanceof TextLyrics tl) {
                        return new BasicAudioLyrics(
                                tl.source(),
                                tl.text(),
                                List.of()
                        );
                    } else if (it instanceof TimedLyrics tl) {
                        return new BasicAudioLyrics(
                                tl.source(),
                                null,
                                tl.lines()
                                        .stream()
                                        .map((line) -> new BasicAudioLyrics.BasicLine(
                                                Duration.of(line.range().start(), ChronoUnit.MILLIS),
                                                Duration.of(line.range().end(), ChronoUnit.MILLIS),
                                                line.line()
                                        ))
                                        .collect(Collectors.toList())
                        );
                    }

                    return null;
                })
                .orElse(null);
    }

    @Override
    public void shutdown() {
        try {
            youtubeClient.close();

            if (geniusClient != null) {
                geniusClient.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
