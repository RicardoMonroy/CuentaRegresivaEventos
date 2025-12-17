#!/bin/bash

# 🚀 Script de Compilación Manual - Cuenta Regresiva Eventos
# Versión: 1.1.2
# Autor: Sistema Automatizado

echo "=============================================="
echo "🚀 COMPILACIÓN MANUAL - Cuenta Regresiva Eventos"
echo "=============================================="

# Verificar Java 17
echo "📋 Verificando Java 17..."
if ! command -v java &> /dev/null; then
    echo "❌ Java no está instalado. Instala Java 17:"
    echo "   Ubuntu/Debian: sudo apt install openjdk-17-jdk"
    echo "   macOS: brew install openjdk@17"
    exit 1
fi

java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
echo "✅ Java encontrado: $java_version"

# Verificar Android SDK
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME no está configurado"
    echo "   Configura la variable ANDROID_HOME pointing a tu SDK de Android"
    exit 1
fi

echo "✅ Android SDK: $ANDROID_HOME"

# Limpiar build anterior
echo "🧹 Limpiando build anterior..."
./gradlew clean

# Compilar Debug APK
echo "🔨 Compilando Debug APK..."
./gradlew assembleDebug --no-daemon --stacktrace

if [ $? -eq 0 ]; then
    echo "✅ Debug APK compilado exitosamente"
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        echo "📁 Ubicación: app/build/outputs/apk/debug/app-debug.apk"
        ls -la app/build/outputs/apk/debug/app-debug.apk
    fi
else
    echo "❌ Error compilando Debug APK"
    exit 1
fi

# Compilar Release APK
echo "🔨 Compilando Release APK..."
./gradlew assembleRelease --no-daemon --stacktrace

if [ $? -eq 0 ]; then
    echo "✅ Release APK compilado exitosamente"
    if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
        echo "📁 Ubicación: app/build/outputs/apk/release/app-release.apk"
        ls -la app/build/outputs/apk/release/app-release.apk
    fi
else
    echo "❌ Error compilando Release APK"
    echo "⚠️  Continuando solo con Debug APK..."
fi

echo ""
echo "=============================================="
echo "✅ COMPILACIÓN COMPLETADA"
echo "=============================================="
echo ""
echo "📱 APKs generados:"
ls -la app/build/outputs/apk/*/app-*.apk 2>/dev/null || echo "   No se encontraron APKs"
echo ""
echo "🌐 Para subir a GitHub Releases:"
echo "1. Ve a: https://github.com/RicardoMonroy/CuentaRegresivaEventos/releases"
echo "2. Clic en 'Create a new release'"
echo "3. Tag version: v0.2.2"
echo "4. Title: Release v0.2.2"
echo "5. Arrastra los APKs aquí"
echo "6. Publish release"
echo ""
echo "📋 Comandos útiles:"
echo "   ./gradlew installDebug     # Instalar en dispositivo conectado"
echo "   ./gradlew connectedAndroidTest  # Ejecutar tests"
echo "=============================================="