# Shake-N-Back

A free, ad-free Dual N-Back working-memory trainer for Android, built with Kotlin and Jetpack Compose.

Each turn a square lights up on a 3×3 grid and a letter is spoken. Tap **Position** when the square matches the one *N* turns ago, and **Sound** when the letter does. Levels go up and down automatically as you improve.

## Features

- Classic dual N-Back (position + audio), with the rules [Brain Workshop](https://brainworkshop.sourceforge.net/) uses:
  - 20 + N trials per session, with exactly 6 targets per modality
  - you advance at ≥ 80%, and drop back after three sessions below 50% at the same level
  - score = hits / (hits + misses + false alarms), pooled across both modalities
- Material 3 UI with a fixed brand palette, dark mode, and edge-to-edge layout
- Animated grid, countdown, answer feedback, haptics, and a results ring
- A chart of your level history, plus today/total/peak stats
- Settings: manual level, time per turn (2–4 s), auto-levelling, feedback, haptics
- Letters are spoken with the system text-to-speech engine, so there are no audio assets
- Hardware keyboard support (`A` = position, `L` = sound)
- Fully offline. No accounts, no tracking, and no permissions beyond the TTS query.

## Project layout

```
core/   Pure-Kotlin game engine (no Android deps), unit tested
        SequenceGenerator · Session (responses + scoring) · LevelRules
app/    Android app (Compose)
        AppViewModel (session loop + navigation) · data/ (DataStore) · audio/ (TTS) · ui/
```

Keeping the engine in `:core` means the game rules can be tested quickly on the JVM, without an emulator.

## Building

Requirements: JDK 17+ and the Android SDK (API 35).

```bash
./gradlew :core:test          # engine unit tests
./gradlew :app:assembleDebug  # APK in app/build/outputs/apk/debug/
./gradlew :app:installDebug   # install on a connected device
```

CI (`.github/workflows/build.yml`) runs the tests on every push and publishes the APK as the rolling `latest-build` pre-release:
<https://github.com/geno-whirl/Shake-N-Back/releases/download/latest-build/shake-n-back-debug.apk>

Debug builds are signed with the committed `app/debug.keystore` (standard Android debug credentials), so new builds install over old ones and keep your progress.

## Roadmap ideas

- **Shake** controls: an optional shake or tilt gesture as an answer, or to start or pause
- More modes: single, triple (+ colour), arithmetic, variable N
- Recorded letter audio as an alternative to TTS
- Richer stats: per-modality trends, streaks, daily reminders
- Tablet and landscape layout, plus Material You dynamic colour as an option
- F-Droid metadata and release signing
