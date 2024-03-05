package me.duncte123.lyrics;

import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.planner.*;
import lavalink.server.config.RateLimitConfig;
import lavalink.server.config.ServerConfig;

import java.util.Optional;

public class HttpClientProvider implements AutoCloseable {
    private final HttpInterfaceManager httpInterfaceManager;

    public HttpClientProvider(AbstractRoutePlanner routePlanner, ServerConfig serverConfig) {
        this.httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();

        if (routePlanner != null) {
            final int retryLimit = Optional.ofNullable(serverConfig.getRatelimit())
                    .map(RateLimitConfig::getRetryLimit)
                    .orElse(-1);

            final YoutubeIpRotatorSetup rotator;

            if (retryLimit < 0) {
                rotator = new YoutubeIpRotatorSetup(routePlanner);
            } else if (retryLimit == 0) {
                rotator = new YoutubeIpRotatorSetup(routePlanner).withRetryLimit(Integer.MAX_VALUE);
            } else {
                rotator = new YoutubeIpRotatorSetup(routePlanner).withRetryLimit(retryLimit);
            }

            rotator.forConfiguration(this.httpInterfaceManager, false).setup();
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
