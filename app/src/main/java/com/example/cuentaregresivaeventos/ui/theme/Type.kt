package com.example.cuentaregresivaeventos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

private val Base = Typography()

// Títulos con más peso para que el nombre de la banda y la cuenta regresiva destaquen
val Typography = Typography(
    displaySmall = Base.displaySmall.copy(fontWeight = FontWeight.Black),
    headlineLarge = Base.headlineLarge.copy(fontWeight = FontWeight.Black),
    headlineMedium = Base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = Base.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.Bold)
)
