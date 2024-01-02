package me.duncte123.lyrics.lavalyrics;

import com.github.topi314.lavalyrics.AudioLyricsManager;
import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.github.topi314.lavalyrics.lyrics.BasicAudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.duncte123.lyrics.GeniusClient;
import me.duncte123.lyrics.LyricsClient;
import me.duncte123.lyrics.lavalink.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    @Override
    public @Nullable AudioLyrics loadLyrics(@NotNull AudioTrack audioTrack) {
        return new BasicAudioLyrics("genus.com", "test", List.of());
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
