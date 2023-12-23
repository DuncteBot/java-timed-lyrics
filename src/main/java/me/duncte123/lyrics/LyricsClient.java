package me.duncte123.lyrics;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.duncte123.lyrics.exception.LyricsNotFoundException;
import me.duncte123.lyrics.model.Lyrics;
import me.duncte123.lyrics.model.SearchTrack;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class LyricsClient {

    public Future<Lyrics> findLyrics(AudioTrack track) {
        try {
            final String videoId;

            if (track instanceof YoutubeAudioTrack ytTrack) {
                videoId = ytTrack.getInfo().identifier;
            } else if (track.getInfo().isrc != null) {
                final var searched = search(track.getInfo().isrc).get();

                if (searched.isEmpty()) {
                    throw new LyricsNotFoundException();
                }

                videoId = searched.get(0).videoId();
            } else {
                final var searched = search(
                        "%s - %s".formatted(track.getInfo().title, track.getInfo().author)
                ).get();

                if (searched.isEmpty()) {
                    throw new LyricsNotFoundException();
                }

                videoId = searched.get(0).videoId();
            }

            return requestLyrics(videoId);
        } catch (InterruptedException | ExecutionException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public Future<Lyrics> requestLyrics(String videoId) {
        throw new LyricsNotFoundException();
    }

    public Future<List<SearchTrack>> search(String query) {
        return search(query, null);
    }

    public Future<List<SearchTrack>> search(String query, String region) {
        throw new LyricsNotFoundException();
    }
}
