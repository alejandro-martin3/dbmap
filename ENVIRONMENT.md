# Entorno de desarrollo - dBMap

Versiones mínimas que deben coincidir entre ordenadores para evitar problemas de compilación.

## Java / JDK

- JDK: OpenJDK 21.0.6 Temurin

## Android Studio

- Android Studio: Quail 1 | 2026.1.1

## Gradle

- Gradle Wrapper: 9.4.1
- Android Gradle Plugin: 9.2.1
- Kotlin usado por Gradle: 2.3.0

## Android SDK

- compileSdk: Android 36.1
- targetSdk: 36
- minSdk: 26
- Android SDK Build Tools: 36.0.0
- Android SDK Platform Tools: 35.0.2

## Proyecto

- Namespace: com.alejandromartin.dbmap
- Application ID: com.alejandromartin.dbmap
- Interfaz: XML
- ViewBinding: activado

## Dependencias principales

- AndroidX Core KTX: 1.10.1
- AndroidX AppCompat: 1.6.1
- AndroidX ConstraintLayout: 2.1.4
- AndroidX Activity KTX: 1.8.0
- AndroidX Navigation Fragment KTX: 2.6.0
- AndroidX Navigation UI KTX: 2.6.0
- Material Components: 1.10.0
- JUnit: 4.13.2

## Comandos de comprobación

```powershell
.\gradlew.bat -v
.\gradlew.bat build
```

## No subir al repositorio

- local.properties
- .idea/
- .gradle/
- build/
- app/build/
