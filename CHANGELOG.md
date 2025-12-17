# 📋 Changelog - Cuenta Regresiva Eventos

Todas las modificaciones importantes a este proyecto serán documentadas en este archivo.

## [1.2.0] - 2025-12-17

### ✨ **NUEVA FUNCIONALIDAD PRINCIPAL: Widget con Actualización Automática**

#### 🔄 **Actualización Automática del Widget**
- **Broadcast receiver implementado**: Widget detecta automáticamente cuando se añaden/modifican/eliminan eventos
- **Actualización inmediata**: El widget se actualiza en menos de 2 segundos después de cambios
- **Comunicación app-widget**: Sistema completo de comunicación bidireccional
- **Gestión de lifecycle**: Registro y desregistro adecuado de receivers

#### 📱 **Mejoras de UI/UX del Widget**
- **ScrollView integrado**: Navegación vertical entre múltiples eventos
- **Soporte para 5 eventos**: Aumentado de 3 a 5 eventos visibles (anteriormente limitado)
- **Layout optimizado**: Mejor aprovechamiento del espacio disponible
- **Textos redimensionados**: Tamaños ajustados para mejor legibilidad
- **Scroll suave**: Navegación intuitiva entre eventos

#### ⏰ **Countdown en Tiempo Real**
- **Actualización periódica**: Widget se actualiza cada 30 segundos automáticamente
- **AlarmManager implementado**: Sistema robusto de actualizaciones programadas
- **Countdown preciso**: Cálculo en tiempo real de días restantes
- **Sin intervención manual**: No requiere refrescado manual

#### 🛠️ **Mejoras Técnicas**
- **EventViewModel modificado**: Añadido `sendWidgetUpdateBroadcast()` para comunicación
- **EventsWidgetProvider mejorado**: Implementado sistema completo de broadcasts
- **Layout rediseñado**: Nuevo widget_events.xml con ScrollView y 5 contenedores
- **Configuración expandida**: events_widget_info.xml con tamaños optimizados

### 📋 **Archivos Modificados/Creados**

#### 🔄 **Archivos Modificados**
- `app/src/main/java/com/example/cuentaregresivaeventos/EventsWidgetProvider.kt` - Lógica de actualización automática
- `app/src/main/java/com/example/cuentaregresivaeventos/ui/EventViewModel.kt` - Broadcast de cambios
- `app/src/main/res/layout/widget_events.xml` - Layout con ScrollView y 5 eventos
- `app/src/main/res/xml/events_widget_info.xml` - Configuración expandida del widget

#### ✨ **Nuevos Archivos**
- `WIDGET_AUTO_UPDATE.md` - Documentación completa de la nueva funcionalidad
- `CHANGELOG.md` - Actualizado con versión 1.2.0

### 🎯 **Funcionalidad Garantizada**
- ✅ **Actualización inmediata** al añadir eventos
- ✅ **Cambios instantáneos** al editar eventos  
- ✅ **Eliminación automática** del widget al borrar eventos
- ✅ **Scroll funcional** para navegar entre eventos
- ✅ **Countdown en tiempo real** cada 30 segundos
- ✅ **Sin intervención manual** requerida

### 📱 **Experiencia de Usuario**
- **Widget completamente automático**: No necesita refrescado manual
- **Información siempre actualizada**: Eventos sincronizados instantáneamente
- **Navegación intuitiva**: Scroll vertical para múltiples eventos
- **Visualización optimizada**: Mejor aprovechamiento del espacio

---

## [1.1.2] - 2025-12-17

### 🔧 **Corrección GitHub Actions**

#### 🚀 **Activación de Release Automática**
- **Nueva tag v0.2.2**: Creada para activar GitHub Actions automáticamente
- **Workflow trigger**: Verificado que el workflow "Android Release" se ejecuta con tags
- **APK upload**: Confirmado proceso de subida de APKs a GitHub Releases
- **Manual trigger**: Verificada opción de activación manual desde GitHub Actions

#### 📱 **Estado de Releases**
- **v0.2.1**: Tag creado pero GitHub Actions no se activó automáticamente
- **v0.2.2**: Nueva tag para forzar activación automática del workflow
- **Fallback**: Opción manual disponible en GitHub Actions > Android Release > Run workflow

### 📋 **Instrucciones para Usuario**
1. **Si v0.2.2 no aparece automáticamente**: Ir a GitHub Actions y ejecutar manualmente
2. **Verificar releases**: https://github.com/RicardoMonroy/CuentaRegresivaEventos/releases
3. **Descargar APKs**: Disponible en cada release después de la compilación

## [1.1.1] - 2025-12-16

### ✨ Nuevas Funcionalidades

#### 🎨 **Widget Mejorado**
- **Widget con scroll**: Implementada funcionalidad de scroll para mostrar múltiples eventos en el widget
- **Colores personalizados**: 
  - Fondo de elementos: `#383434`
  - Caja de fecha: `#A69D9D`
- **Diseño de fecha optimizado**:
  - Días mostrados en fuente más grande
  - Meses y años debajo de los días
  - Caja de fecha en el lado derecho del widget
- **Formato de fecha en español**: Implementado formato completamente localizado con nombres de meses en español

#### 📱 **Modal Dialog Mejorado**
- **Botón de edición reposicionado**: Movido desde la parte superior al área de acciones inferior
- **Visualización de imagen en pantalla completa**: Nueva funcionalidad para ver imágenes de eventos en fullscreen
- **Organización mejorada**: Mejor estructura visual y espaciado de elementos
- **Iconos actualizados**: Reemplazados iconos no disponibles por Material Icons compatibles

#### 🛠️ **Mejoras Técnicas**
- **Java 17**: Migración completa a Java 17 para mejor compatibilidad
- **DateFormatter personalizado**: Nueva clase para formateo de fechas en español
- **Manejo de errores robusto**: Implementado manejo de errores en widget y operaciones de base de datos
- **Estados de fallback**: Agregados estados de carga y error en el widget

### 🔧 **Mejoras Técnicas**

#### 🏗️ **Arquitectura**
- **Modularización mejorada**: Separación clara entre UI, data y utilidades
- **Coroutines optimizadas**: Mejor manejo de operaciones asíncronas
- **Repository pattern**: Implementación robusta del patrón repository

#### 📦 **Dependencias**
- **Kotlin 2.0**: Actualización a la última versión de Kotlin
- **Jetpack Compose**: Optimizaciones en componentes UI
- **Material Icons**: Selección de iconos compatibles

### 🐛 **Correcciones de Bugs**

#### 🔨 **Compilación**
- **Errores de Java 25**: Resuelto conflicto con Java 25.0.1 (versión incompatible)
- **Errores de iconos**: Solucionados errores "Unresolved reference" para Material icons
- **Type mismatch**: Corregidos errores Long vs Int en DateFormatter
- **Gradle sync**: Mejorada sincronización de dependencias

#### 📱 **Widget**
- **"Can't load widget"**: Resuelto problema de carga del widget
- **ScrollView issues**: Corregido problema con ScrollView en widget
- **Size constraints**: Ajustados tamaños de widget para mejor visualización

### 📋 **Archivos Modificados/Creados**

#### ✨ **Nuevos Archivos**
- `.github/workflows/android-ci.yml` - Pipeline de CI/CD para compilación automática
- `.github/workflows/android-release.yml` - Pipeline de releases automáticas
- `app/src/main/java/com/example/cuentaregresivaeventos/util/DateFormatter.kt` - Utilidades de formateo de fecha
- `CONFIGURACION_ANDROID_STUDIO.md` - Guía de configuración de Android Studio
- `COMANDOS_UTILES.md` - Comandos útiles para desarrollo
- `setup_env.sh` - Script de configuración automática del entorno
- `CHANGELOG.md` - Este archivo de cambios

#### 🔄 **Archivos Modificados**
- `README.md` - Documentación completa actualizada con badges y información detallada
- `app/src/main/res/layout/widget_events.xml` - Layout rediseñado del widget
- `app/src/main/java/com/example/cuentaregresivaeventos/EventsWidgetProvider.kt` - Lógica mejorada del widget
- `app/src/main/java/com/example/cuentaregresivaeventos/ui/screens/CountdownScreen.kt` - Modal dialog mejorado
- `app/src/main/res/xml/events_widget_info.xml` - Configuración del widget
- `gradle.properties` - Configuración de Java 17

#### 🗑️ **Archivos Eliminados**
- `app/src/main/java/com/example/cuentaregresivaeventos/adapter/WidgetEventAdapter.kt` - Removido (causaba conflictos de tipos)

### 🎯 **Mejoras de Performance**
- **Compilación más rápida**: Optimizado tiempo de compilación con Java 17
- **Widget responsivo**: Mejor rendimiento del widget con scroll
- **Memoria optimizada**: Reducido uso de memoria en operaciones de base de datos
- **UI fluida**: Mejor responsividad en animaciones y transiciones

### 📱 **Compatibilidad**
- **Android SDK**: Compatible con API 24+ (Android 7.0+)
- **Material Design 3**: Implementación completa de Material You
- **Spanish Localization**: Soporte completo para formato de fecha en español
- **Accesibilidad**: Mejorado soporte para lectores de pantalla

### 🚀 **CI/CD Implementado**
- **GitHub Actions**: Pipeline completo de integración continua
- **Compilación automática**: Build automático en cada push
- **Testing automatizado**: Tests unitarios y de instrumentación
- **Releases automáticas**: Generación automática de releases en GitHub
- **Artifact upload**: APKs subidos automáticamente como artifacts

## [1.0.0] - Versión Inicial

### ✨ Funcionalidades Base
- Lista de eventos con countdown en tiempo real
- CRUD completo de eventos
- Almacenamiento local con Room
- Modal dialog con detalles de eventos
- Widget básico de pantalla de inicio
- Soporte para imágenes de eventos

### 🏗️ Tecnologías Base
- Kotlin con Jetpack Compose
- Room Database
- ViewModel + LiveData
- Material Design 3
- Coroutines para operaciones asíncronas

---

## 📝 Formato de Changelog

Este changelog sigue el formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/) y se adhiere al [Versionado Semántico](https://semver.org/lang/es/).

### Tipos de Cambios
- **✨ Nuevas Funcionalidades** - para nuevas features
- **🔧 Mejoras** - para cambios en features existentes
- **🐛 Correcciones** - para bug fixes
- **🔨 Compilación** - para cambios en herramientas de build
- **📦 Dependencias** - para actualizaciones de dependencias
- **🗑️ Removido** - para features removidos
- **🔒 Seguridad** - para vulnerabilidades corregidas

### Versionado
- **MAJOR** - Cambios incompatibles en la API
- **MINOR** - Funcionalidades agregadas de forma compatible
- **PATCH** - Correcciones de bugs compatibles