package com.example.cuentaregresivaeventos

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.cuentaregresivaeventos.data.EventDatabase
import com.example.cuentaregresivaeventos.util.DateFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventsWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.cuentaregresivaeventos.ACTION_UPDATE_WIDGET"
        const val ACTION_EVENTS_CHANGED = "com.example.cuentaregresivaeventos.ACTION_EVENTS_CHANGED"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // El refresco periódico ahora lo hace updatePeriodMillis; se cancela la alarma de 30 s de versiones previas
        cancelLegacyAlarm(context)
        updateAllWidgets(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelLegacyAlarm(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_EVENTS_CHANGED) {
            // Events were added/updated/deleted, update all widgets
            updateAllWidgets(context)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        // Called when widget options change (size, etc.)
        updateAllWidgets(context)
    }

    private fun cancelLegacyAlarm(context: Context) {
        val intent = Intent(context, EventsWidgetProvider::class.java).setAction(ACTION_UPDATE_WIDGET)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pendingIntent)
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, EventsWidgetProvider::class.java))
        if (appWidgetIds.isEmpty()) return

        // goAsync mantiene vivo el receiver hasta terminar la consulta; sin él Android puede matar el proceso
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_events)
                // Intent to open the app
                val openApp = PendingIntent.getActivity(
                    context, 0, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, openApp)
                try {
                    val now = System.currentTimeMillis()
                    val events = EventDatabase.getInstance(context).eventDao().getAllEventsList()
                        .filter { it.dateTimeMillis > now }
                        .take(6) // Show up to 6 events
                    if (events.isEmpty()) showEmptyState(views) else updateEventsInWidget(views, events)
                } catch (e: Exception) {
                    e.printStackTrace()
                    showEmptyState(views)
                }
                appWidgetManager.updateAppWidget(appWidgetIds, views)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun updateEventsInWidget(views: RemoteViews, events: List<com.example.cuentaregresivaeventos.data.EventEntity>) {
        try {
            // Define all possible event container IDs and their text view IDs (up to 6 events)
            // Updated to include location text view: [title, date, location, days]
            val eventContainers = arrayOf(
                R.id.event_1_container to arrayOf(R.id.event_1_title, R.id.event_1_date, R.id.event_1_location, R.id.event_1_days),
                R.id.event_2_container to arrayOf(R.id.event_2_title, R.id.event_2_date, R.id.event_2_location, R.id.event_2_days),
                R.id.event_3_container to arrayOf(R.id.event_3_title, R.id.event_3_date, R.id.event_3_location, R.id.event_3_days),
                R.id.event_4_container to arrayOf(R.id.event_4_title, R.id.event_4_date, R.id.event_4_location, R.id.event_4_days),
                R.id.event_5_container to arrayOf(R.id.event_5_title, R.id.event_5_date, R.id.event_5_location, R.id.event_5_days),
                R.id.event_6_container to arrayOf(R.id.event_6_title, R.id.event_6_date, R.id.event_6_location, R.id.event_6_days)
            )

            // Hide all containers first
            eventContainers.forEach { (containerId, _) ->
                views.setViewVisibility(containerId, android.view.View.GONE)
            }

            // Update visible events
            events.forEachIndexed { index, event ->
                if (index < eventContainers.size) {
                    try {
                        val (containerId, textIds) = eventContainers[index]
                        
                        // Safe date formatting with fallbacks
                        val formattedDate = try {
                            DateFormatter.formatDateTimeSpanish(event.dateTimeMillis)
                        } catch (e: Exception) {
                            "Fecha inválida"
                        }
                        
                        // Format location with place and city
                        val locationText = try {
                            val place = event.place?.trim() ?: ""
                            val city = event.city?.trim() ?: ""
                            when {
                                place.isNotEmpty() && city.isNotEmpty() -> "$place, $city"
                                place.isNotEmpty() -> place
                                city.isNotEmpty() -> city
                                else -> ""
                            }
                        } catch (e: Exception) {
                            ""
                        }
                        
                        val daysRemaining = try {
                            val now = System.currentTimeMillis()
                            val diff = event.dateTimeMillis - now
                            if (diff <= 0) {
                                "0"
                            } else {
                                val totalSeconds = diff / 1000
                                val days = totalSeconds / (24 * 3600)
                                days.toString()
                            }
                        } catch (e: Exception) {
                            "?"
                        }

                        // Show the container
                        views.setViewVisibility(containerId, android.view.View.VISIBLE)
                        
                        // Set text for title, date, location, and days
                        views.setTextViewText(textIds[0], event.title ?: "Sin título")
                        views.setTextViewText(textIds[1], formattedDate)
                        views.setTextViewText(textIds[2], locationText)
                        views.setTextViewText(textIds[3], daysRemaining)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Skip this event if there's an error
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If there's a general error, show empty state
            showEmptyState(views)
        }
    }

    private fun showEmptyState(views: RemoteViews) {
        // Show only the first container with "No events" message
        views.setViewVisibility(R.id.event_1_container, android.view.View.VISIBLE)
        views.setTextViewText(R.id.event_1_title, "No hay eventos")
        views.setTextViewText(R.id.event_1_date, "Agrega eventos desde la app")
        views.setTextViewText(R.id.event_1_location, "")
        views.setTextViewText(R.id.event_1_days, "--")
        
        // Hide all other containers (6 events max)
        views.setViewVisibility(R.id.event_2_container, android.view.View.GONE)
        views.setViewVisibility(R.id.event_3_container, android.view.View.GONE)
        views.setViewVisibility(R.id.event_4_container, android.view.View.GONE)
        views.setViewVisibility(R.id.event_5_container, android.view.View.GONE)
        views.setViewVisibility(R.id.event_6_container, android.view.View.GONE)
    }
}