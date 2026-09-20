# Ding Dong — Watch Party & Player

Sync video watching for your crew. Start or join a room, play MP4/MKV/MP3/WebM (local files or
links, including YouTube), and everyone's playback stays in step. Chat, reactions and a shared
playlist travel with the room — a link you paste reaches everyone, including people who join
later.

Built with **Kotlin** and **Jetpack Compose Multiplatform**, on top of the MPV engine.

## Features

- **Sync rules** — play / pause / seek stay in sync across the room, compatible with the
  official Syncplay desktop client (syncplay.pl servers: 8995–8999).
- **MPV player** — single bundled engine: nearly every container and codec, styled subtitles,
  chapters, precise seeking. Aspect ratios + pan-scan in full screen, GPU / hardware-decoding
  options in the in-room Player → Advanced settings.
- **Media** — add local files (mp3, mkv, mp4, webm, …) or paste a link (YouTube / MP4 / HLS).
  Shared with the whole room, late joiners included.
- **Chat** — text, GIF and emoji, with **swipe-reply**: swipe horizontally across any message
  to quote it in your reply.
- **Full screen** — landscape watch mode with the chat panel and floating messages; messages
  stay visible while the picture plays.
- Host your own server from the app, invite links, themes, gestures, PiP.

## Building the APK

The `build-apk.yml` workflow builds a debug APK on every push and attaches it to a GitHub
Release — download it from the **Releases** tab, or trigger a build from **Actions**.

To build locally you need JDK 21, the Android SDK (platform 37.2, build-tools 37.0.0) and
NDK 29.0.14206865:

```
./gradlew :androidApp:assembleDebug
```

The APK lands in `androidApp/build/outputs/apk/debug/`.

## Credits & License

Ding Dong is based on [Synkplay (syncplay-mobile)](https://github.com/yuroyami/syncplay-mobile)
by **yuroyami**, an independent client for the Syncplay protocol (syncplay.pl), distributed
under the **AGPL-3.0** license — see [LICENSE](LICENSE). It stays compatible with the official
[Syncplay](https://syncplay.pl) desktop client.

Modifications for Ding Dong: app identity and icon, MPV-only engine list, swipe-to-reply chat.
