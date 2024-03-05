package me.duncte123.lyrics.lavalink;

import com.github.topi314.lavalyrics.LyricsManager;
import com.github.topi314.lavalyrics.api.LyricsManagerConfiguration;
import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner;
import lavalink.server.config.ServerConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class JavaLyricsManagerConfiguration implements LyricsManagerConfiguration {
    private final JavaAudioLyricsManager javaAudioLyricsManager;

    public JavaLyricsManagerConfiguration(Config config, AbstractRoutePlanner routePlanner, ServerConfig serverConfig) {
        this.javaAudioLyricsManager = new JavaAudioLyricsManager(config, routePlanner, serverConfig);
    }

    @NotNull
    @Override
    public LyricsManager configure(@NotNull LyricsManager lyricsManager) {
        lyricsManager.registerLyricsManager(this.javaAudioLyricsManager);

        return lyricsManager;
    }
}
