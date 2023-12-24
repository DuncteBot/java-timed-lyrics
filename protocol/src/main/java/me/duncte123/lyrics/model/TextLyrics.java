package me.duncte123.lyrics.model;

public record TextLyrics(Track track, String source, String text) implements Lyrics {
    @Override
    public String getType() {
        return "text";
    }
}
