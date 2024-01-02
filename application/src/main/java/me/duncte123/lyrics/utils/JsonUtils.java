package me.duncte123.lyrics.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;

import java.util.stream.Collectors;

public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJsonString(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getRunningText(JsonBrowser browser, String key) {
        final var runs = browser.get(key).get("runs");

        if (runs.isNull() || !runs.isList()) {
            return null;
        }

        return runs.values()
                .stream()
                .map((it) -> it.get("text").text())
                .collect(Collectors.joining(" "));
    }
}
