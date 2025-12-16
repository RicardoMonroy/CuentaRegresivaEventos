# 📱 Cuenta Regresiva de Eventos

[![Android CI](https://github.com/RicardoMonroy/cuenta-regresiva-eventos/actions/workflows/android-ci.yml/badge.svg)](https://github.com/RicardoMonroy/cuenta-regresiva-eventos/actions/workflows/android-ci.yml)
[![Android Release](https://github.com/RicardoMonroy/cuenta-regresiva-eventos/actions/workflows/android-release.yml/badge.svg)](https://github.com/RicardoMonroy/cuenta-regresiva-eventos/actions/workflows/android-release.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg)](https://kotlinlang.org/)
[![Android SDK](https://img.shields.io/badge/Android-SDK%2034-green.svg)](https://developer.android.com/about/versions/14)
[![License](https://img.shields.io/badge/License-Educational-yellow.svg)](LICENSE)

> **Aplicación Android moderna para gestionar eventos con conteo regresivo en tiempo real**

Aplicación Android construida con **Kotlin**, **Jetpack Compose**, **Room**, **ViewModel** y las últimas tecnologías de desarrollo Android. Incluye widget personalizable, formato de fecha en español y una interfaz de usuario completamente optimizada.

## ✨ Características Principales

### 🎯 **Funcionalidades Core**
- 📋 **Lista de eventos** ordenados por fecha con cuenta regresiva en tiempo real
- ⏰ **Countdown automático** que se actualiza cada segundo
- 🎨 **Widget de pantalla de inicio** con scroll para múltiples eventos
- 📅 **Formato de fecha en español** completamente localizado
- 📷 **Imágenes de eventos** con visualización en pantalla completa
- 📱 **CRUD completo** para gestionar eventos

### 🎨 **Mejoras Recientes**
- ✅ **Widget mejorado** con scroll funcional y diseño moderno
- ✅ **Colores personalizados** (#383434, #A69D9D) para mejor UX
- ✅ **Modal dialog reorganizado** con botón de edición reposicionado
- ✅ **Visualización de imagen en pantalla completa**
- ✅ **Tipografía optimizada** para títulos y fechas
- ✅ **Formateo de fecha español** con métodos personalizados

### 🏗️ **Arquitectura Técnica**
- **Kotlin 2.0** con programación asíncrona
- **Jetpack Compose** para UI moderna
- **Room Database** para persistencia local
- **ViewModel + LiveData** para arquitectura MVVM
- **Coroutines** para operaciones asíncronas
- **Material Design 3** para componentes UI

## 📱 Screenshots

| Pantalla Principal | Widget | Modal Dialog |
|-------------------|--------|--------------|
| ![Main Screen](screenshots/main.png) | ![Widget](screenshots/widget.png) | ![Modal](screenshots/modal.png) |

## 🚀 Instalación Rápida

### Opción 1: APK Pre-compilado
Descarga e instala directamente:
- **[📱 Descargar APK Debug](./releases/download/latest/app-debug.apk)**
- **[📦 Descargar APK Release](./releases/download/latest/app-release.apk)**

### Opción 2: Desde Código Fuente

#### Prerrequisitos
- **Android Studio** (Giraffe o superior)
- **JDK 17** (incluido en el proyecto)
- **Android SDK** API 24+
- **Emulador** o dispositivo físico Android

#### Pasos de Instalación

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/RicardoMonroy/cuenta-regresiva-eventos.git
   cd cuenta-regresiva-eventos
   ```

2. **Configurar entorno (automático):**
   ```bash
   chmod +x setup_env.sh
   ./setup_env.sh
   ```

3. **Abrir en Android Studio:**
   - File → Open → Seleccionar carpeta del proyecto
   - Esperar sincronización de Gradle

4. **Configurar Java 17 en Android Studio:**
   - File → Settings → Build Tools → Gradle
   - Gradle JDK → Add JDK → Seleccionar `jdk-17.0.2`

5. **Ejecutar aplicación:**
   - Conectar emulador/dispositivo
   - Run → Run 'app' (o Shift+F10)

## 🛠️ Comandos de Desarrollo

### Compilación
```bash
# Configurar entorno
./setup_env.sh

# Compilar APK de desarrollo
./gradlew assembleDebug

# Compilar APK de producción
./gradlew assembleRelease

# Limpiar build
./gradlew clean

# Instalar en emulador
./gradlew installDebug
```

### Testing
```bash
# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests de instrumentación
./gradlew connectedAndroidTest

# Verificar linting
./gradlew lint
```

### Comandos ADB Útiles
```bash
# Ver dispositivos conectados
adb devices

# Instalar APK manualmente
adb install app/build/outputs/apk/debug/app-debug.apk

# Ver logs en tiempo real
adb logcat | grep "CuentaRegresiva"

# Limpiar datos de la app
adb shell pm clear com.example.cuentaregresivaeventos
```

## 📁 Estructura del Proyecto

```
📦 Cuenta Regresiva Eventos
├── 📂 .github/workflows/          # GitHub Actions CI/CD
│   ├── android-ci.yml            # Pipeline de CI
│   └── android-release.yml       # Pipeline de releases
├── 📂 app/src/main/
│   ├── 📂 java/com/example/cuentaregresivaeventos/
│   │   ├── 📂 data/              # Room Database
│   │   │   ├── EventDao.kt
│   │   │   ├── EventDatabase.kt
│   │   │   ├── EventEntity.kt
│   │   │   └── EventRepository.kt
│   │   ├── 📂 ui/                # UI Layer
│   │   │   ├── EventViewModel.kt
│   │   │   └── 📂 screens/
│   │   │       └── CountdownScreen.kt
│   │   ├── 📂 util/              # Utilidades
│   │   │   └── DateFormatter.kt  # Formateo fecha español
│   │   ├── EventsWidgetProvider.kt
│   │   └── MainActivity.kt
│   ├── 📂 res/
│   │   ├── 📂 layout/
│   │   │   └── widget_events.xml # Layout del widget
│   │   └── 📂 xml/
│   │       └── events_widget_info.xml
│   └── AndroidManifest.xml
├── 📂 gradle/
│   └── libs.versions.toml        # Versiones dependencias
├── 📄 CONFIGURACION_ANDROID_STUDIO.md
├── 📄 COMANDOS_UTILES.md
├── 📄 setup_env.sh               # Script configuración
└── 📄 README.md
```

## 🎯 Configuración del Widget

### Características del Widget
- **Scroll automático** para múltiples eventos
- **Colores personalizados** (#383434, #A69D9D)
- **Formato fecha español** con días, meses y años
- **Actualización en tiempo real** cada minuto

### Agregar Widget a Pantalla de Inicio
1. Mantener presionada la pantalla de inicio
2. Seleccionar "Widgets"
3. Buscar "Events Widget"
4. Arrastrar a la posición deseada

## 🔄 CI/CD con GitHub Actions

### Workflows Configurados

#### 🔧 **Android CI** (`android-ci.yml`)
- ✅ Ejecuta en cada push a `main`/`develop`
- ✅ Compila con JDK 17
- ✅ Ejecuta tests unitarios y lint
- ✅ Genera APKs de debug y release
- ✅ Sube artifacts automáticamente

#### 🚀 **Android Release** (`android-release.yml`)
- ✅ Se ejecuta en tags (`v*`)
- ✅ Genera releases automáticos en GitHub
- ✅ Incluye APKs en las releases
- ✅ Descripción automática con changelog

### Estado de Builds
![Build Status](https://github.com/RicardoMonroy/cuenta-regresiva-eventos/workflows/Android%20CI/badge.svg)

## 📋 Requisitos del Sistema

### Desarrollo
| Componente | Versión Mínima | Recomendada |
|------------|----------------|-------------|
| Android Studio | Giraffe 2022.3.1 | Hedgehog 2023.1.1 |
| JDK | 17 | 17 |
| Kotlin | 2.0 | 2.0 |
| Gradle | 8.0 | 8.13 |
| Android SDK | API 24 | API 34 |

### Ejecución
| Requisito | Valor |
|-----------|--------|
| Android Version | 7.0+ (API 24) |
| RAM | 2GB mínimo |
| Almacenamiento | 100MB |
| Permisos | Ninguno especial |

## 🤝 Contribución

### Cómo Contribuir
1. Fork el proyecto
2. Crear branch para feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

### Guidelines
- ✅ Seguir convenciones de código Kotlin
- ✅ Incluir tests para nuevas funcionalidades
- ✅ Actualizar documentación relevante
- ✅ Verificar que CI pase exitosamente

## 📄 Licencia

Este proyecto se distribuye únicamente con **fines educativos**. 

**⚠️ Importante:** Respeta siempre las licencias y derechos de autor de terceros. Las librerías y recursos utilizados tienen sus propias licencias que deben ser respetadas.

## 📞 Soporte

### 🐛 Reportar Bugs
- Usar [GitHub Issues](https://github.com/RicardoMonroy/cuenta-regresiva-eventos/issues)
- Incluir pasos para reproducir
- Especificar versión de Android y dispositivo

### 💡 Solicitar Features
- Crear [GitHub Discussion](https://github.com/RicardoMonroy/cuenta-regresiva-eventos/discussions)
- Describir funcionalidad deseada
- Considerar viabilidad técnica

## 📈 Roadmap

### 🎯 Versión 1.1 (Planificada)
- [ ] Sincronización con calendario
- [ ] Notificaciones push
- [ ] Backup/restore en la nube
- [ ] Modo oscuro completo
- [ ] Widgets adicionales

### 🎯 Versión 1.2 (Futuro)
- [ ] Compartir eventos
- [ ] Exportar a CSV/ICS
- [ ] Temas personalizables
- [ ] Soporte multi-idioma completo
- [ ] Analytics de uso

---

<div align="center">

**Desarrollado con ❤️ usando Kotlin y Jetpack Compose**

[![Star on GitHub](https://img.shields.io/github/stars/RicardoMonroy/cuenta-regresiva-eventos?style=social)](https://github.com/RicardoMonroy/cuenta-regresiva-eventos)
[![Fork on GitHub](https://img.shields.io/github/forks/RicardoMonroy/cuenta-regresiva-eventos?style=social)](https://github.com/RicardoMonroy/cuenta-regresiva-eventos/fork)

</div>
