package com.example.cuentaregresivaeventos

import com.example.cuentaregresivaeventos.util.DateFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormatterTest {
    private val now = 1_000_000_000_000L
    private val minute = 60_000L
    private val hour = 60 * minute
    private val day = 24 * hour

    @Test
    fun widgetCountdown_unidades() {
        assertEquals("12" to "días", DateFormatter.widgetCountdown(now + 12 * day + 5 * hour, now))
        assertEquals("1" to "día", DateFormatter.widgetCountdown(now + day, now))
        assertEquals("23" to "horas", DateFormatter.widgetCountdown(now + day - 1, now))
        assertEquals("1" to "hora", DateFormatter.widgetCountdown(now + hour, now))
        assertEquals("59" to "min", DateFormatter.widgetCountdown(now + hour - 1, now))
        assertEquals("0" to "min", DateFormatter.widgetCountdown(now - day, now))
    }
}
