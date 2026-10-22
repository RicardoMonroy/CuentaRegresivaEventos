package com.example.cuentaregresivaeventos

import com.example.cuentaregresivaeventos.util.EventSearch
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class EventSearchTest {
    private val march2027 = Calendar.getInstance().apply { set(2027, Calendar.MARCH, 18, 18, 0) }.timeInMillis

    @Test
    fun arma_busquedas_codificadas() {
        assertEquals(
            "https://www.google.com/search?q=Overkill+concierto+CDMX+2027",
            EventSearch.google("Overkill", "CDMX", march2027)
        )
        assertEquals(
            "https://www.google.com/search?q=Sigur+R%C3%B3s+M%C3%A9xico+2027+boletos+precio",
            EventSearch.tickets("Sigur Rós", "", march2027)
        )
        assertEquals("https://www.ticketmaster.com.mx/search?q=AC%2FDC", EventSearch.ticketmaster(" AC/DC "))
    }
}
