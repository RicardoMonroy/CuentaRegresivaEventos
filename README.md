# Cuenta Regresiva de Eventos

Aplicación Android para gestionar eventos con conteo regresivo, construida con **Kotlin**, **Jetpack Compose**, **Room** y **ViewModel**.

## Características

- Lista de eventos ordenados por fecha con cuenta regresiva en tiempo real.
- Cada evento incluye título, lugar, ciudad, descripción opcional, fecha y hora.
- Posibilidad de adjuntar una imagen (flyer) del evento.
- CRUD completo: crear, editar y eliminar eventos.
- Al tocar un evento se muestra un diálogo con todos los detalles.
- Almacenamiento local usando Room.

## Instalación (APK)

Puedes instalar la app directamente en tu dispositivo Android descargando el APK:

- [Descargar APK](./releases/download/v0.1.0/app-debug.apk)

Activa la instalación de orígenes desconocidos en tu dispositivo si es necesario y abre el archivo APK para instalarlo.

## Requisitos de desarrollo

- Android Studio (Koala o superior).
- Kotlin 2.x.
- Min SDK 24.
- Gradle y JDK configurados (se incluye en Android Studio).

## Ejecutar el proyecto

1. Clonar el repositorio:
    ```
    git clone https://github.com/RicardoMonroy/cuenta-regresiva-eventos.git

    cd cuenta-regresiva-eventos
    ```
   
2. Abrir la carpeta en Android Studio (**File > Open…**).

3. Esperar a que Gradle sincronice y luego ejecutar la app en un emulador o dispositivo físico.

## Licencia

Este proyecto se distribuye únicamente con fines educativos. Respeta siempre las licencias y derechos de autor de terceros.
