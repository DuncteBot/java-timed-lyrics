package me.duncte123.lyrics.lavalink;

import com.github.topi314.lavalyrics.LyricsManager;
import com.github.topi314.lavalyrics.api.LyricsManagerConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class JavaLyricsManagerConfiguration implements LyricsManagerConfiguration {
    private final JavaAudioLyricsManager javaAudioLyricsManager;

    public JavaLyricsManagerConfiguration(Config config, AudioPlayerManager audioPlayerManager) {
        this.javaAudioLyricsManager = new JavaAudioLyricsManager(config, audioPlayerManager);
    }

    @NotNull
    @Override
    public LyricsManager configure(@NotNull LyricsManager lyricsManager) {
        lyricsManager.registerLyricsManager(this.javaAudioLyricsManager);

        return lyricsManager;
    }
}
