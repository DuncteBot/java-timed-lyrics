package me.duncte123.lyrics.model;

import java.util.function.Function;

public record Context(Client client) {
    public static final Context DEFAULT_MOBILE_REQUEST = new Context(new Client());
    public static final Function<String, Context> DEFAULT_MOBILE_REQUEST_WITH_REGION = (hl) -> new Context(new Client(hl));
}
