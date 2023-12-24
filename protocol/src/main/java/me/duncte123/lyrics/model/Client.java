package me.duncte123.lyrics.model;

public record Client(String clientName, String clientVersion, String hl) {
    public Client(String clientName, String clientVersion) {
        this(clientName, clientVersion, null);
    }

    public Client(String hl) {
        this("ANDROID_MUSIC", "6.31.55", hl);
    }

    public Client() {
        this(null);
    }
}
