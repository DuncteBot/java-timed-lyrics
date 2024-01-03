package me.duncte123.lyrics;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;

public class HttpClientProvider implements AutoCloseable {
    private final HttpInterfaceManager httpInterfaceManager;

    public HttpClientProvider(AudioPlayerManager playerManager) {
        final YoutubeAudioSourceManager sourceManager = playerManager.source(YoutubeAudioSourceManager.class);

        if (sourceManager != null) {
            this.httpInterfaceManager = (HttpInterfaceManager) sourceManager.getMainHttpConfiguration();
        } else {
            this.httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();
        }
    }

    public HttpInterface getHttpInterface() {
        return httpInterfaceManager.getInterface();
    }

    @Override
    public void close() {
        ExceptionTools.closeWithWarnings(this.httpInterfaceManager);
    }
}
