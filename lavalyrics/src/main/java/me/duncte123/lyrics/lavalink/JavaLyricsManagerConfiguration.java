package me.duncte123.lyrics.lavalink;

import com.github.topi314.lavalyrics.LyricsManager;
import com.github.topi314.lavalyrics.api.LyricsManagerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class JavaLyricsManagerConfiguration implements LyricsManagerConfiguration {
    private final JavaAudioLyricsManager javaAudioLyricsManager;

    public JavaLyricsManagerConfiguration(Config config) {
        this.javaAudioLyricsManager = new JavaAudioLyricsManager(config);
    }

    @NotNull
    @Override
    public LyricsManager configure(@NotNull LyricsManager lyricsManager) {
        lyricsManager.registerLyricsManager(this.javaAudioLyricsManager);

        return lyricsManager;
    }
}
