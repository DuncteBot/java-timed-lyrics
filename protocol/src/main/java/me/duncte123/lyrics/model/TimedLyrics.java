package me.duncte123.lyrics.model;

import java.util.List;

public record TimedLyrics(Track track, String source, List<Line> lines) {
}
