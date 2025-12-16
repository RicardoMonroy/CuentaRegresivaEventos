package com.example.cuentaregresivaeventos

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.cuentaregresivaeventos.R
import com.example.cuentaregresivaeventos.data.EventDatabase
import com.example.cuentaregresivaeventos.util.DateFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Update all widgets
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first instance of this widget is created
        super.onEnabled(context)
        // Update all widgets when the app is first installed
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            android.content.ComponentName(context, EventsWidgetProvider::class.java)
        )
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        // Called when widget options change (size, etc.)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            val views = RemoteViews(context.packageName, R.layout.widget_events)

            // Intent to open the app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            // Load events and update the widget
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = EventDatabase.getInstance(context)
                    val events = db.eventDao().getAllEventsList().take(3) // Limit to 3 events for better stability
    
                    withContext(Dispatchers.Main) {
                        try {
                            updateEventsInWidget(views, events)
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Update with empty state if there's an error
                            showEmptyState(views)
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        showEmptyState(views)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If there's a critical error, try to update with a basic widget
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_events)
                showEmptyState(views)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    private fun updateEventsInWidget(views: RemoteViews, events: List<com.example.cuentaregresivaeventos.data.EventEntity>) {
        try {
            // Define all possible event container IDs and their text view IDs (only 3 events)
            val eventContainers = arrayOf(
                R.id.event_1_container to arrayOf(R.id.event_1_title, R.id.event_1_date, R.id.event_1_days),
                R.id.event_2_container to arrayOf(R.id.event_2_title, R.id.event_2_date, R.id.event_2_days),
                R.id.event_3_container to arrayOf(R.id.event_3_title, R.id.event_3_date, R.id.event_3_days)
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
                        
                        // Set text for title, date, and days
                        views.setTextViewText(textIds[0], event.title ?: "Sin título")
                        views.setTextViewText(textIds[1], formattedDate)
                        views.setTextViewText(textIds[2], daysRemaining)
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
        views.setTextViewText(R.id.event_1_days, "--")
        
        // Hide all other containers (only 3 events now)
        views.setViewVisibility(R.id.event_2_container, android.view.View.GONE)
        views.setViewVisibility(R.id.event_3_container, android.view.View.GONE)
    }
}