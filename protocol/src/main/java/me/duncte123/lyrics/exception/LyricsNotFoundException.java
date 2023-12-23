package me.duncte123.lyrics.exception;

public class LyricsNotFoundException extends RuntimeException {
    public LyricsNotFoundException() {
        super("Lyrics were not found");
    }
}
