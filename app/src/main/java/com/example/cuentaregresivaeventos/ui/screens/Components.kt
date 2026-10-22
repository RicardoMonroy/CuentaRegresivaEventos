package com.example.cuentaregresivaeventos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cuentaregresivaeventos.ui.EventUi
import com.example.cuentaregresivaeventos.util.DateFormatter
import java.util.Locale

// Oscurece la parte baja del flyer para que el texto encima siempre se lea
val FlyerScrim = Brush.verticalGradient(
    0f to Color.Transparent,
    0.35f to Color.Black.copy(alpha = 0.15f),
    0.6f to Color.Black.copy(alpha = 0.7f),
    1f to Color.Black.copy(alpha = 0.95f)
)

fun EventUi.locationText(): String =
    listOf(place, city).map { it.trim() }.filter { it.isNotEmpty() }.joinToString(", ")

/** Flyer del evento; sin imagen muestra un degradado con la inicial del título. */
@Composable
fun FlyerImage(
    imagePath: String?,
    title: String,
    modifier: Modifier = Modifier,
    grayscale: Boolean = false
) {
    if (imagePath.isNullOrEmpty()) {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer)
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.take(1).uppercase(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f)
            )
        }
    } else {
        AsyncImage(
            model = imagePath,
            contentDescription = "Flyer de $title",
            contentScale = ContentScale.Crop,
            colorFilter = if (grayscale) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
            modifier = modifier
        )
    }
}

/** Cuenta regresiva en vivo en bloques; `now` se lee aquí para que solo esto se recomponga cada segundo. */
@Composable
fun CountdownBlocks(dateTimeMillis: Long, now: () -> Long, modifier: Modifier = Modifier) {
    val parts = DateFormatter.countdownParts(dateTimeMillis, now())
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CountdownBlock(parts.days.toString(), if (parts.days == 1L) "día" else "días")
        CountdownBlock(String.format(Locale.ROOT, "%02d", parts.hours), "horas")
        CountdownBlock(String.format(Locale.ROOT, "%02d", parts.minutes), "min")
        CountdownBlock(String.format(Locale.ROOT, "%02d", parts.seconds), "seg")
    }
}

@Composable
private fun RowScope.CountdownBlock(value: String, label: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
            color = Color.White
        )
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f))
    }
}

/** Píldora compacta "147 días" para las tarjetas. */
@Composable
fun DaysPill(dateTimeMillis: Long, now: () -> Long, modifier: Modifier = Modifier) {
    val (number, unit) = DateFormatter.widgetCountdown(dateTimeMillis, now())
    Text(
        text = "$number $unit",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
