# dBMap: Ruido en Tiempo Real

dBMap es una aplicación Android desarrollada en Kotlin para medir ruido ambiental de forma aproximada, guardar mediciones anónimas y visualizar los datos en un mapa mediante zonas coloreadas.

## Requisitos

- Android 8.0 o superior.
- Conexión a internet.
- Permisos de micrófono y ubicación.
- Archivo APK de la aplicación o proyecto abierto en Android Studio.

## Instalación desde APK

1. Copiar el archivo `dbMap.apk` en el dispositivo Android.
2. Abrir el archivo desde el gestor de archivos.
3. Permitir la instalación desde fuentes externas si Android lo solicita.
4. Pulsar en **Instalar**.
5. Abrir la aplicación desde el menú del dispositivo.

## Ejecución desde Android Studio

1. Abrir el proyecto en Android Studio.
2. Esperar a que Gradle sincronice las dependencias.
3. Conectar un dispositivo Android o iniciar un emulador.
4. Pulsar **Run** para instalar y ejecutar la app.

## Uso básico

La aplicación tiene cuatro pantallas principales:

- **Medición**: permite medir el ruido ambiental usando el micrófono. La app muestra el nivel aproximado en decibelios, la zona aproximada y el estado del guardado.
- **Mapa**: muestra las zonas medidas sobre OpenStreetMap, usando colores según el nivel medio de ruido.
- **Histórico**: muestra los últimos datos agrupados por zona y franja temporal.
- **Ajustes**: permite activar o desactivar el centrado automático del mapa y el aviso de privacidad.

## Privacidad

dBMap no guarda audio, usuarios, correos, identificadores personales ni coordenadas exactas. La ubicación se convierte en una zona aproximada mediante geohash reducido antes de guardar la medición.

## Pruebas

El proyecto incluye pruebas unitarias con JUnit para comprobar la lógica de `ZoneManager` y los modelos de datos principales.