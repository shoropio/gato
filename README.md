# Gato

![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-UI-6750A4?logo=materialdesign&logoColor=white)
![Room](https://img.shields.io/badge/Room-Persistencia-0F9D58)
![Gradle](https://img.shields.io/badge/Gradle-9.5.1-02303A?logo=gradle&logoColor=white)
![Release](https://img.shields.io/github/v/release/shoropio/gato?label=release)

Gato es un juego Android de tres en raya con interfaz neon cyberpunk, multijugador local, modos contra IA, historial de partidas, logros, respuesta sonora y estilos visuales personalizables.

## Características

- Modo jugador contra IA con varios niveles de dificultad.
- Modo jugador contra jugador local.
- Modo espectador IA contra IA.
- Estadísticas, rachas y logros persistentes con Room.
- Interfaz Jetpack Compose con paneles y controles de radio cero.
- Pantalla de créditos con reconocimientos de código abierto.

## Requisitos

- Android Studio con soporte para JDK 21.
- Android SDK 36.
- Un dispositivo o emulador con Android 7.0/API 24 o superior.

El proyecto incluye Gradle Wrapper y criterios de JVM para el Gradle Daemon, por lo que las máquinas compatibles pueden usar o descargar automáticamente un JDK adecuado.

## Compilación

```powershell
.\gradlew.bat testDebugUnitTest :app:packageDebug --warning-mode all
```

El APK debug se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Proyecto

- Paquete: `com.shoropio.gato`
- SDK mínimo: 24
- SDK objetivo: 36
- Interfaz: Jetpack Compose y Material 3
- Persistencia: Room
- Pruebas: JUnit, Robolectric y Roborazzi

## Créditos

Esta aplicación usa bibliotecas Android y Kotlin de código abierto, incluyendo AndroidX, Jetpack Compose, Room, Kotlin Coroutines, KSP, Moshi, OkHttp, Retrofit, Robolectric y Roborazzi.

© 2026 Shoropio Corporation. Todos los derechos reservados.
