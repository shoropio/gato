# Gato

Gato is an Android tic-tac-toe game with a cyberpunk neon interface, local multiplayer, AI modes, match history, achievements, sound feedback, and customizable visual styles.

## Features

- Player vs AI with multiple difficulty levels.
- Local player vs player mode.
- AI vs AI spectator mode.
- Persistent stats, streaks, and achievements with Room.
- Neon Compose UI with zero-radius panels and controls.
- Credits screen with open source acknowledgements.

## Requirements

- Android Studio with JDK 21 support.
- Android SDK 36.
- A device or emulator running Android 7.0/API 24 or newer.

The project includes Gradle wrapper files and Gradle Daemon JVM criteria, so supported machines can use or download a compatible JDK automatically.

## Build

```powershell
.\gradlew.bat testDebugUnitTest :app:packageDebug --warning-mode all
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Project

- Package: `com.shoropio.gato`
- Minimum SDK: 24
- Target SDK: 36
- UI: Jetpack Compose and Material 3
- Persistence: Room
- Tests: JUnit, Robolectric, Roborazzi

## Credits

This application uses open source Android and Kotlin libraries including AndroidX, Jetpack Compose, Room, Kotlin Coroutines, KSP, Moshi, OkHttp, Retrofit, Robolectric, and Roborazzi.

© 2026 Shoropio Corporation. Todos los derechos reservados.
