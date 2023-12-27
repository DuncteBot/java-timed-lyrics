package me.duncte123.lyrics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TimedLyrics(Track track, String source, List<Line> lines) implements Lyrics {
    @Override
    public String getType() {
        return "timed";
    }
}
