package me.duncte123.lyrics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TextLyrics(Track track, String source, String text) implements Lyrics {
    @Override
    public String getType() {
        return "text";
    }
}
