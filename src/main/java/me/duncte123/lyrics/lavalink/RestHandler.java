package me.duncte123.lyrics.lavalink;

import lavalink.server.io.SocketServer;
import me.duncte123.lyrics.LyricsClient;
import me.duncte123.lyrics.exception.LyricsNotFoundException;
import me.duncte123.lyrics.model.Lyrics;
import me.duncte123.lyrics.model.SearchTrack;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static lavalink.server.util.UtilKt.socketContext;

@RestController
public class RestHandler {
    private final LyricsClient client = new LyricsClient();

    private final SocketServer socketServer;
    private final Config config;

    public RestHandler(SocketServer socketServer, Config config) {
        this.socketServer = socketServer;
        this.config = config;
    }

    @GetMapping(value = "/v4/lyrics/{videoId}")
    public Lyrics getLyrics(@PathVariable String videoId) {
        try {
            return client.requestLyrics(videoId).get();
        } catch (LyricsNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, null, e);
        }
    }

    @GetMapping(value = "/v4/lyrics/search")
    public Future<List<SearchTrack>> search(@RequestParam String query) {
        return client.search(query, config.getCountryCode());
    }

    @GetMapping(value = "/v4/sessions/{sessionId}/players/{guildId}/lyrics")
    public Future<Lyrics> getLyricsOfPlayingTrack(@PathVariable String sessionId, @PathVariable long guildId) {
        final var playingTrack = socketContext(socketServer, sessionId)
                .getPlayer(guildId)
                .getTrack();

        if (playingTrack == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not currently playing anything");
        }

        try {
            return client.findLyrics(playingTrack);
        } catch (LyricsNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, e);
        }
    }


}
