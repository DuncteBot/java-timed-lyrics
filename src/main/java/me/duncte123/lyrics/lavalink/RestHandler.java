package me.duncte123.lyrics.lavalink;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner;
import lavalink.server.config.ServerConfig;
import lavalink.server.io.SocketServer;
import me.duncte123.lyrics.GeniusClient;
import me.duncte123.lyrics.HttpClientProvider;
import me.duncte123.lyrics.LyricsClient;
import me.duncte123.lyrics.exception.LyricsNotFoundException;
import me.duncte123.lyrics.model.Lyrics;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

import static lavalink.server.util.UtilKt.socketContext;

@RestController
public class RestHandler {
    private final LyricsClient ytClient;
    private final GeniusClient geniusClient;

    private final SocketServer socketServer;
    private final Config config;

    public RestHandler(SocketServer socketServer, Config config, AbstractRoutePlanner routePlanner, ServerConfig serverConfig) {
        this.socketServer = socketServer;
        this.config = config;

        final HttpClientProvider httpProvider = new HttpClientProvider(routePlanner, serverConfig);

        this.ytClient = new LyricsClient(httpProvider);

        final String geniusApiKey = config.getGeniusApiKey();

        if (geniusApiKey == null || geniusApiKey.isBlank()) {
            geniusClient = null;
        } else {
            geniusClient = new GeniusClient(geniusApiKey, httpProvider);
        }
    }

    @GetMapping(value = "/v4/lyrics/{videoId}")
    public Lyrics getLyrics(@PathVariable("videoId") String videoId) {
        try {
            return ytClient.requestLyrics(videoId).get();
        } catch (Exception e) {
            if (e.getCause() instanceof LyricsNotFoundException lnfe) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, lnfe.getMessage());
            }

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping(value = "/v4/lyrics/search")
    public Object search(
            @RequestParam("query") String query,
            @RequestParam(name = "source", required = false, defaultValue = "youtube") String source
    ) {
        try {
            return switch (source.toLowerCase(Locale.ROOT)) {
                case "youtube" -> ytClient.search(query, config.getCountryCode()).get();
                case "genius" -> geniusClient.search(query).get();
                default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown source type: " + source);
            };
        } catch (Exception e) {
            if (e.getCause() instanceof LyricsNotFoundException lnfe) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, lnfe.getMessage());
            }

            if (e instanceof ResponseStatusException rse) {
                throw rse;
            }

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping(value = "/v4/sessions/{sessionId}/players/{guildId}/lyrics")
    public Lyrics getLyricsOfPlayingTrack(@PathVariable("sessionId") String sessionId, @PathVariable("guildId") long guildId) throws Exception {
        final var playingTrack = socketContext(socketServer, sessionId)
                .getPlayer(guildId)
                .getTrack();

        if (playingTrack == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not currently playing anything");
        }

        try {
            return ytClient.findLyrics(playingTrack).get();
        } catch (Exception e) {
            if (e.getCause() instanceof LyricsNotFoundException lnfe) {
                if (geniusClient != null) {
                    return geniusClient.findLyrics(playingTrack).get();
                }

                throw new ResponseStatusException(HttpStatus.NOT_FOUND, lnfe.getMessage());
            }

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }


}
