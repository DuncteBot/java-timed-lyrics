package me.duncte123.lyrics.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(name="text", value=TextLyrics.class),
        @JsonSubTypes.Type(name="timed", value=TimedLyrics.class)
})
public interface Lyrics {
    String getType();
}
