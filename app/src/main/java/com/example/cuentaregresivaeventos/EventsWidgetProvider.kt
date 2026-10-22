package com.example.cuentaregresivaeventos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.cuentaregresivaeventos.data.EventDatabase
import com.example.cuentaregresivaeventos.data.EventEntity
import com.example.cuentaregresivaeventos.util.DateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// El nombre de la clase se conserva para no quitar los widgets ya colocados en la pantalla de inicio
class EventsWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EventsWidget()
}

private data class WidgetEvent(val entity: EventEntity, val image: Bitmap?)

class EventsWidget : GlanceAppWidget() {

    companion object {
        private val SMALL = DpSize(110.dp, 110.dp)
        private val LIST = DpSize(250.dp, 180.dp)
        private const val MAX_EVENTS = 10
        // Los bitmaps viajan dentro de RemoteViews, que tiene límite de memoria: miniaturas pequeñas
        private const val HERO_PX = 480
        private const val THUMB_PX = 160
    }

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, LIST))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val now = System.currentTimeMillis()
        val events = withContext(Dispatchers.IO) {
            EventDatabase.getInstance(context).eventDao().getAllEventsList()
                .filter { it.dateTimeMillis > now }
                .take(MAX_EVENTS)
                .mapIndexed { index, entity ->
                    WidgetEvent(entity, entity.imagePath?.let { loadThumbnail(it, if (index == 0) HERO_PX else THUMB_PX) })
                }
        }

        provideContent {
            GlanceTheme {
                WidgetContent(events, now)
            }
        }
    }

    private fun loadThumbnail(path: String, maxPx: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxPx) sample *= 2
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}

@Composable
private fun WidgetContent(events: List<WidgetEvent>, now: Long) {
    val size = LocalSize.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(24.dp)
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        when {
            events.isEmpty() -> EmptyState()
            // Widget chico o un solo evento: el flyer ocupa todo
            events.size == 1 || size.width < 250.dp || size.height < 180.dp -> NextEventHero(events.first(), now)
            else -> EventList(events, now)
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = GlanceModifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Sin eventos próximos",
            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            "Toca para agregar uno",
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
        )
    }
}

@Composable
private fun NextEventHero(event: WidgetEvent, now: Long, modifier: GlanceModifier = GlanceModifier.fillMaxSize()) {
    val (number, unit) = DateFormatter.widgetCountdown(event.entity.dateTimeMillis, now)
    val textColor = if (event.image != null) ColorProvider(Color.White) else GlanceTheme.colors.onPrimaryContainer

    Box(
        modifier = modifier
            .cornerRadius(24.dp)
            .background(GlanceTheme.colors.primaryContainer)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        if (event.image != null) {
            Image(
                provider = ImageProvider(event.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = GlanceModifier.fillMaxSize()
            )
            // Degradado oscuro abajo para que el texto se lea sobre cualquier flyer
            Box(modifier = GlanceModifier.fillMaxSize().background(ImageProvider(R.drawable.widget_scrim))) {}
        }
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(14.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(number, style = TextStyle(color = textColor, fontSize = 44.sp, fontWeight = FontWeight.Bold))
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    unit,
                    modifier = GlanceModifier.padding(bottom = 8.dp),
                    style = TextStyle(color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                )
            }
            Spacer(GlanceModifier.height(2.dp))
            Text(
                event.entity.title,
                maxLines = 2,
                style = TextStyle(color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                listOf(DateFormatter.formatShortDateTimeSpanish(event.entity.dateTimeMillis), event.entity.city.trim())
                    .filter { it.isNotEmpty() }.joinToString(" · "),
                maxLines = 1,
                style = TextStyle(color = textColor, fontSize = 12.sp)
            )
        }
    }
}

@Composable
private fun EventList(events: List<WidgetEvent>, now: Long) {
    Column(modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 12.dp)) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Próximos eventos",
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                "${events.size}",
                style = TextStyle(color = GlanceTheme.colors.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            )
        }
        LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
            // El próximo evento va como banner con su flyer; el resto en filas
            item(itemId = events.first().entity.id) {
                Column {
                    NextEventHero(events.first(), now, GlanceModifier.fillMaxWidth().height(150.dp))
                    Spacer(GlanceModifier.height(8.dp))
                }
            }
            items(events.drop(1), itemId = { it.entity.id }) { event ->
                Column {
                    EventRow(event, now)
                    Spacer(GlanceModifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: WidgetEvent, now: Long) {
    val entity = event.entity
    val (number, unit) = DateFormatter.widgetCountdown(entity.dateTimeMillis, now)
    val location = listOf(entity.place, entity.city).map { it.trim() }.filter { it.isNotEmpty() }.joinToString(", ")

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(16.dp)
            .background(GlanceTheme.colors.surfaceVariant)
            .padding(8.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (event.image != null) {
            Image(
                provider = ImageProvider(event.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = GlanceModifier.size(52.dp).cornerRadius(12.dp)
            )
        } else {
            Box(
                modifier = GlanceModifier.size(52.dp).cornerRadius(12.dp).background(GlanceTheme.colors.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    entity.title.take(1).uppercase(),
                    style = TextStyle(color = GlanceTheme.colors.onSecondaryContainer, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
        Spacer(GlanceModifier.width(10.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                entity.title,
                maxLines = 1,
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                DateFormatter.formatShortDateTimeSpanish(entity.dateTimeMillis),
                maxLines = 1,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
            )
            if (location.isNotEmpty()) {
                Text(
                    location,
                    maxLines = 1,
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
                )
            }
        }
        Spacer(GlanceModifier.width(8.dp))
        Column(
            modifier = GlanceModifier
                .width(58.dp)
                .cornerRadius(12.dp)
                .background(GlanceTheme.colors.primaryContainer)
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                number,
                style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
            Text(unit, style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer, fontSize = 11.sp))
        }
    }
}
