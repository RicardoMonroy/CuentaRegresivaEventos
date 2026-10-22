package com.example.cuentaregresivaeventos.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    
    private val spanishMonths = arrayOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
    
    private val spanishShortMonths = arrayOf(
        "ene", "feb", "mar", "abr", "may", "jun",
        "jul", "ago", "sep", "oct", "nov", "dic"
    )
    
    /**
     * Formatea la fecha en formato: "DD de MES de YYYY"
     * Ejemplo: "15 de diciembre de 2024"
     */
    fun formatDateSpanish(dateTimeMillis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateTimeMillis
        }
        
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        
        return "$day de ${spanishMonths[month]} de $year"
    }
    
    /**
     * Formatea la fecha y hora en formato: "DD de MES de YYYY, HH:mm"
     * Ejemplo: "15 de diciembre de 2024, 14:30"
     */
    fun formatDateTimeSpanish(dateTimeMillis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateTimeMillis
        }
        
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        return "$day de ${spanishMonths[month]} de $year, ${String.format(Locale.ROOT, "%02d:%02d", hour, minute)}"
    }
    
    /**
     * Formatea fecha corta para el widget: "DD mes · HH:mm"
     * Ejemplo: "15 dic · 21:00"
     */
    fun formatShortDateTimeSpanish(dateTimeMillis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateTimeMillis
        }

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)

        return "$day ${spanishShortMonths[month]} · ${formatTimeForWidget(dateTimeMillis)}"
    }

    /**
     * Cuenta regresiva para el widget como (número, unidad)
     * Ejemplo: ("12", "días"), ("5", "horas"), ("30", "min")
     */
    fun widgetCountdown(dateTimeMillis: Long, now: Long = System.currentTimeMillis()): Pair<String, String> {
        val totalMinutes = maxOf(dateTimeMillis - now, 0L) / 60_000
        val days = totalMinutes / (24 * 60)
        val hours = totalMinutes / 60

        return when {
            days >= 1 -> days.toString() to if (days == 1L) "día" else "días"
            hours >= 1 -> hours.toString() to if (hours == 1L) "hora" else "horas"
            else -> totalMinutes.toString() to "min"
        }
    }
    
    /**
     * Formatea la hora para el widget: "HH:mm"
     */
    fun formatTimeForWidget(dateTimeMillis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateTimeMillis
        }
        
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        return String.format(Locale.ROOT, "%02d:%02d", hour, minute)
    }
    
    /**
     * Formatea el countdown en formato legible
     * Ejemplo: "25 días, 14 horas, 30 minutos"
     */
    fun formatCountdownSpanish(dateTimeMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = dateTimeMillis - now
        
        if (diff <= 0) return "Evento pasado"
        
        val totalSeconds = diff / 1000
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        
        val parts = mutableListOf<String>()
        
        if (days > 0) {
            parts.add("$days ${if (days == 1L) "día" else "días"}")
        }
        if (hours > 0) {
            parts.add("$hours ${if (hours == 1L) "hora" else "horas"}")
        }
        if (minutes > 0 || parts.isEmpty()) {
            parts.add("$minutes ${if (minutes == 1L) "minuto" else "minutos"}")
        }
        
        return parts.joinToString(", ")
    }
}
