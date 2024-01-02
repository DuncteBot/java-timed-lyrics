package me.duncte123.lyrics.lavalink;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "plugins.lyrics")
public class Config {
    private String countryCode = null;
    private String geniusApiKey = null;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getGeniusApiKey() {
        return geniusApiKey;
    }

    public void setGeniusApiKey(String geniusApiKey) {
        this.geniusApiKey = geniusApiKey;
    }
}
