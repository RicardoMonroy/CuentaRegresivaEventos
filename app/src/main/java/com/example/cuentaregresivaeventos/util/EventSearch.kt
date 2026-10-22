package com.example.cuentaregresivaeventos.util

import java.net.URLEncoder
import java.util.Calendar

/** Búsquedas web ya armadas para completar la info de un evento sin APIs de pago. */
object EventSearch {

    fun google(title: String, city: String, dateTimeMillis: Long): String =
        googleUrl(listOf(title, "concierto", city.ifBlank { "México" }, yearOf(dateTimeMillis)))

    fun tickets(title: String, city: String, dateTimeMillis: Long): String =
        googleUrl(listOf(title, city.ifBlank { "México" }, yearOf(dateTimeMillis), "boletos precio"))

    fun ticketmaster(title: String): String =
        "https://www.ticketmaster.com.mx/search?q=" + encode(title.trim())

    private fun googleUrl(parts: List<String>): String =
        "https://www.google.com/search?q=" + encode(parts.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" "))

    private fun yearOf(millis: Long) = Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.YEAR).toString()

    private fun encode(text: String) = URLEncoder.encode(text, "UTF-8")
}
