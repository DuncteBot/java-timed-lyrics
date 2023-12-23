package me.duncte123.lyrics.model;

import java.util.List;

public record Track(String title, String author, String album, List<AlbumArt> albumArt) {
}
