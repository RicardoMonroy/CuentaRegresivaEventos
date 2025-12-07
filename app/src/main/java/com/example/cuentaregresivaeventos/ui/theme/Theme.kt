package com.example.cuentaregresivaeventos.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun CuentaRegresivaEventosTheme(
    darkTheme: Boolean = true,          // forzamos oscuro
    dynamicColor: Boolean = false,      // lo desactivamos por simplicidad
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme   // siempre dark

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
