package me.duncte123.lyrics.model.request;

import me.duncte123.lyrics.model.Context;

public record SearchRequest(Context context, String query, String params) {
}
