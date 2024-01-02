# Lyrics.java
A very simple lyrics client based on YouTube and [Lyrics.Kt](https://github.com/DRSchlaubi/lyrics.kt)

## Differences from Lyrics.kt
The biggest difference between this version and the kotlin version is the filesize.
At the time of writing, lyrics.kt plugin is about `7 MB` in size, while this plugin is about `34 KiB` in size. A massive reduction.

The second difference is the search endpoint. 
Instead of putting the query into the path of the url, this plugin opted to use query parameters for it.
Making the endpoint more restfull.

# Feature overview
- Small plugin size
- Lyrics from YouTube
- Automatically get lyrics based on the currently playing track
- Timestamped lyrics so you can highlight the current line.
- Optional support for genius lyrics if none are found on YouTube.
- Support for [LavaLyrics](https://github.com/topi314/LavaLyrics) (see the end of the readme)

# Using with Lavalink

Replace x.y.z with the current version [![](https://jitpack.io/v/DuncteBot/java-timed-lyrics.svg)](https://jitpack.io/#DuncteBot/java-timed-lyrics)

```yaml
lavalink:
  plugins:
    - dependency: "com.github.DuncteBot.java-timed-lyrics:java-lyrics-plugin:x.y.z"
      repository: "https://jitpack.io"
plugins:
  lyrics:
    countryCode: de #country code for resolving isrc tracks
    geniusApiKey: "Your Genius Client Access Token" # leave this out to disable genius searching. Get your api key (Client Access Token) from https://genius.com/api-clients
```

## API for clients
```json5
// GET /v4/lyrics/{videoId} (youtube lyrics only)
// GET /v4/sessions/{sessionId}/players/{guildId}/lyrics
// /v4/lyrics/search?query=...&source=genius (genius will always return the first result of text lyrics, default source is YouTube see below for different response)
// Please note that the "album" key will be null for genius.

{
  // can also be text
  "type": "timed",
  "track": {
    "title": "We Got the Moves",
    "author": "Electric Callboy",
    "album": "We Got the Moves",
    "albumArt": [
      {
        "url": "https://lh3.googleusercontent.com/rDaGBvVRyBbHKJuQFFfIUuCLU36OuHHTjDz2u9xDwbIgD2MWM_P6L2L01IOOtoJvi7ks43OFeCqx0cRp=w60-h60-l90-rj",
        "height": 60,
        "width": 60
      }
    ]
  },
  "source": "LyricFind",
  // Only present for type text
  "text": "<lyrics>",
  // Only present for type timed
  "lines": [
    {
      "line": "♪",
      "range": {
        "start": 0,
        // start timestamp in ms since track start
        "end": 6510
        // end timestamp in ms since track start
      }
    },
    {
      "line": "Summer mood",
      "range": {
        "start": 6510,
        "end": 7330
      }
    }
  ]
}
```
```json5
// /v4/lyrics/search?query=...
// /v4/lyrics/search?query=...&source=youtube (youtube is default)

[
  {"videoId": "UVXvQtm6ji0", "title": "We Got The moves"}
]
```

# Using with lavalyrics

To use this plugin with lavalyrics you need to include a different plugin. Please do not include both the main plugin and the lavalyrics plugin as they will conflict with each other.
The Yml is as follows:

```yaml
lavalink:
  plugins:
    - dependency: "com.github.DuncteBot.java-timed-lyrics:lavalyrics:x.y.z"
      repository: "https://jitpack.io"
plugins:
  lyrics:
    countryCode: de #country code for resolving isrc tracks
    geniusApiKey: "Your Genius Client Access Token" # leave this out to disable genius searching. Get your api key (Client Access Token) from https://genius.com/api-clients
```
