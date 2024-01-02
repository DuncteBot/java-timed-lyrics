package me.duncte123.lyrics.lavalink;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lavalink.server.io.SocketServer;
import me.duncte123.lyrics.GeniusClient;
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

    public RestHandler(SocketServer socketServer, Config config, AudioPlayerManager audioPlayerManager) {
        this.socketServer = socketServer;
        this.config = config;
        this.ytClient = new LyricsClient(audioPlayerManager);

        final String geniusApiKey = config.getGeniusApiKey();

        if (geniusApiKey == null || geniusApiKey.isBlank()) {
            geniusClient = null;
        } else {
            geniusClient = new GeniusClient(geniusApiKey);
        }
    }

    @GetMapping(value = "/v4/lyrics/{videoId}")
    public Lyrics getLyrics(@PathVariable String videoId) {
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
    public Object search(@RequestParam String query, @RequestParam(required = false, defaultValue = "youtube") String source) {
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
    public Lyrics getLyricsOfPlayingTrack(@PathVariable String sessionId, @PathVariable long guildId) throws Exception {
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
