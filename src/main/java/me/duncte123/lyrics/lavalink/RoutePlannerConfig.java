package me.duncte123.lyrics.lavalink;

import com.sedmelluq.lava.extensions.youtuberotator.planner.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class RoutePlannerConfig {
    @Bean
    public AbstractRoutePlanner routePlanner() {
        return new RotatingNanoIpRoutePlanner(
            Collections.emptyList(),
            addr -> false
        );
    }
}