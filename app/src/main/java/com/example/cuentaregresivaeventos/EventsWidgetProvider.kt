package com.example.cuentaregresivaeventos

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.cuentaregresivaeventos.R
import com.example.cuentaregresivaeventos.data.EventDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class EventsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_events)

        // Intent to open the app
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        // Load events
        CoroutineScope(Dispatchers.IO).launch {
            val db = EventDatabase.getInstance(context)
            val events = db.eventDao().getAllEventsList().take(5)

            withContext(Dispatchers.Main) {
                // Update the views
                val eventIds = arrayOf(
                    R.id.event_1 to arrayOf(R.id.event_1_title, R.id.event_1_date, R.id.event_1_countdown),
                    R.id.event_2 to arrayOf(R.id.event_2_title, R.id.event_2_date, R.id.event_2_countdown),
                    R.id.event_3 to arrayOf(R.id.event_3_title, R.id.event_3_date, R.id.event_3_countdown),
                    R.id.event_4 to arrayOf(R.id.event_4_title, R.id.event_4_date, R.id.event_4_countdown),
                    R.id.event_5 to arrayOf(R.id.event_5_title, R.id.event_5_date, R.id.event_5_countdown)
                )

                for (i in 0..4) {
                    val (containerId, textIds) = eventIds[i]
                    if (i < events.size) {
                        val event = events[i]
                        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(event.dateTimeMillis))
                        val remaining = calculateRemaining(event.dateTimeMillis)
                        views.setViewVisibility(containerId, android.view.View.VISIBLE)
                        views.setTextViewText(textIds[0], event.title)
                        views.setTextViewText(textIds[1], dateStr)
                        views.setTextViewText(textIds[2], remaining)
                    } else {
                        views.setViewVisibility(containerId, android.view.View.GONE)
                    }
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun calculateRemaining(dateTimeMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = dateTimeMillis - now
        if (diff <= 0) return "Pasado"
        val totalSeconds = diff / 1000
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        return "${days}d ${hours}h ${minutes}m"
    }
}