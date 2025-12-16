# 🏷️ Guía de Tags y Releases - Cuenta Regresiva Eventos

## ¿Cómo funcionan los tags?

### ❌ **Los tags NO se crean automáticamente**

Los tags deben ser **creados manualmente** en Git, pero una vez creados, **GitHub Actions generará automáticamente** las releases con APKs.

## 📋 **Proceso Completo**

### 1️⃣ **Crear Tag Manualmente**

```bash
# Desde tu directorio del proyecto
git tag v0.2.1
git push origin v0.2.1
```

### 2️⃣ **GitHub Actions Se Activa Automáticamente**

Cuando hagas push del tag:
- ✅ Workflow `android-release.yml` se ejecuta automáticamente
- ✅ Genera APKs (debug y release)
- ✅ Crea release en GitHub con los APKs
- ✅ Sube artifacts automáticamente

## 🎯 **Versión Actual: v0.2.0**

Tu proyecto está actualmente en **v0.2.0**. Aquí tienes las opciones:

### **Opción A: Mantener v0.2.0**
Si v0.2.0 es la versión actual estable:
```bash
git tag v0.2.0
git push origin v0.2.0
```

### **Opción B: Crear Nueva Versión**
Para una nueva versión con todas las mejoras:
```bash
git tag v1.0.0
git push origin v1.0.0
```

## 🚀 **Comandos Prácticos**

### **Ver Tags Existentes**
```bash
git tag --list
```

### **Crear Tag con Mensaje**
```bash
git tag -a v1.0.0 -m "Versión 1.0.0 con widget mejorado y CI/CD"
git push origin v1.0.0
```

### **Crear Tag y Push en Una Línea**
```bash
git tag v1.0.0 && git push origin v1.0.0
```

### **Eliminar Tag (si te equivocas)**
```bash
git tag -d v1.0.0          # Eliminar local
git push origin :refs/tags/v1.0.0  # Eliminar remoto
```

## 📱 **Qué Sucede Después del Tag**

### **GitHub Actions Workflow:**
1. **Detecta el tag** `v*`
2. **Descarga el código** del tag
3. **Configura Java 17** automáticamente
4. **Compila debug y release** APKs
5. **Crea release** en GitHub con:
   - 📱 app-debug.apk
   - 📦 app-release.apk
   - 📝 Descripción automática con changelog
6. **Sube artifacts** para descarga

### **Resultado en GitHub:**
- ✅ **Nueva release** en la pestaña "Releases"
- ✅ **APKs disponibles** para descarga
- ✅ **Badges actualizados** en README
- ✅ **Changelog automático**

## 🎯 **Recomendación para v0.2.0**

Dado que mencionas estar en v0.2.0, te sugiero:

```bash
# Crear tag para la versión actual con todas las mejoras
git tag v1.0.0 -m "Versión 1.0.0: Widget mejorado, CI/CD completo, README renovado"

# Push del tag (esto activa GitHub Actions automáticamente)
git push origin v1.0.0
```

## 📋 **Secuencia Recomendada**

1. **Verificar cambios:**
   ```bash
   git status
   git add .
   git commit -m "Preparación para release v1.0.0"
   ```

2. **Crear tag:**
   ```bash
   git tag v1.0.0 -m "Versión 1.0.0 con widget mejorado y CI/CD"
   ```

3. **Push todo:**
   ```bash
   git push origin main
   git push origin v1.0.0
   ```

4. **Verificar en GitHub:**
   - Ir a la pestaña "Actions" para ver el workflow ejecutándose
   - Ir a "Releases" para ver la nueva release creada

## ⚡ **Comando Rápido (Todo en Uno)**

```bash
git add . && git commit -m "Preparación release v1.0.0" && git tag v1.0.0 -m "Versión 1.0.0 con widget mejorado y CI/CD" && git push origin main && git push origin v1.0.0
```

## 🎉 **Resultado Final**

Después del push del tag verás:
- ✅ **GitHub Actions ejecutándose** automáticamente
- ✅ **APKs compilados** en ~5-10 minutos
- ✅ **Nueva release** creada en GitHub
- ✅ **Badges actualizados** en README
- ✅ **Artifacts disponibles** para descarga

---
**En resumen: Los tags se crean manualmente, pero las releases se generan automáticamente por GitHub Actions.**