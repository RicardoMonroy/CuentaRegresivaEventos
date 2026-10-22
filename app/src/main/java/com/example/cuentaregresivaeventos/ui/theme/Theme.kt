package com.example.cuentaregresivaeventos.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ConcertColorScheme = darkColorScheme(
    primary = Ember,
    onPrimary = OnEmber,
    primaryContainer = EmberContainer,
    onPrimaryContainer = OnEmberContainer,
    secondary = Gold,
    tertiary = Magenta,
    background = Night,
    surface = NightSurface,
    surfaceVariant = NightSurfaceVariant
)

@Composable
fun CuentaRegresivaEventosTheme(
    // Siempre oscuro: los flyers lucen mejor sobre fondo oscuro; colores del fondo de pantalla en Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        ConcertColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
