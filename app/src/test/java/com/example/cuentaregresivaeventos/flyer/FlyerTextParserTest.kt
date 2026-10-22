package com.example.cuentaregresivaeventos.flyer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FlyerTextParserTest {
    private val today = LocalDate.of(2026, 10, 22)

    private fun lines(vararg pairs: Pair<String, Int>) = pairs.map { OcrLine(it.first, it.second) }

    @Test
    fun gira_con_varias_fechas_elige_mexico_y_toma_el_año_suelto() {
        val data = FlyerTextParser.parse(
            lines(
                "CKConcerts PRESENTA" to 20,
                "OVERKILL" to 180,
                "BURNING UP" to 60,
                "LATIN AMERICA 2027" to 50,
                "BUENOS AIRES, AR" to 30, "FEB 28" to 20,
                "PANAMA, PA" to 30, "MAR 16" to 20,
                "MEXICO, MX" to 30, "MAR 18" to 20
            ),
            today
        )
        assertEquals("Overkill", data.title)
        assertEquals(Triple(2027, 3, 18), Triple(data.year, data.month, data.day))
        assertEquals("CDMX", data.city)
    }

    @Test
    fun flyer_local_en_español_con_lugar_hora_y_boletos() {
        val data = FlyerTextParser.parse(
            lines(
                "LIFELOVER" to 150,
                "SÁBADO 1 DE MAYO" to 40,
                "FORO 28 · CDMX" to 30,
                "PUERTAS 17:00  SHOW 18:00" to 25,
                "BOLETOS EN ETICKET $650" to 20
            ),
            today
        )
        assertEquals("Lifelover", data.title)
        assertEquals(Triple(2027, 5, 1), Triple(data.year, data.month, data.day))
        assertEquals(18 to 0, data.hour to data.minute)
        assertEquals("Foro 28", data.place)
        assertEquals("CDMX", data.city)
        assertTrue(data.notes!!.contains("ETICKET"))
    }

    @Test
    fun hora_del_show_en_linea_propia_gana_a_puertas() {
        val data = FlyerTextParser.parse(lines("BANDA" to 100, "PUERTAS 7 PM" to 20, "SHOW 9:30 PM" to 20), today)
        assertEquals(21 to 30, data.hour to data.minute)
    }

    @Test
    fun fecha_numerica_y_fecha_de_este_año_aun_futura() {
        assertEquals(
            Triple(2026, 12, 5),
            FlyerTextParser.parse(lines("X" to 1, "05/12/2026" to 10), today).let { Triple(it.year, it.month, it.day) }
        )
        assertEquals(
            Triple(2026, 11, 15),
            FlyerTextParser.parse(lines("15 NOV" to 10), today).let { Triple(it.year, it.month, it.day) }
        )
    }

    @Test
    fun sin_datos_reconocibles() {
        val data = FlyerTextParser.parse(lines("??" to 10), today)
        assertTrue(data.isEmpty)
    }

    @Test
    fun json_del_modelo_con_nulls_y_texto_alrededor() {
        val data = FlyerTextParser.parseModelJson(
            """```json
            {"artist": "Overkill", "date": "2027-03-18", "time": null, "venue": "Pepsi Center", "city": "CDMX", "notes": "null"}
            ```"""
        )
        assertEquals("Overkill", data.title)
        assertEquals(Triple(2027, 3, 18), Triple(data.year, data.month, data.day))
        assertNull(data.hour)
        assertEquals("Pepsi Center", data.place)
        assertNull(data.notes)
    }

    @Test
    fun orElse_completa_sin_mezclar_fecha() {
        val ai = FlyerData(title = "Overkill", year = 2027, month = 3, day = 18)
        val ocr = FlyerData(title = "OVER KILL", year = 2027, month = 2, day = 28, hour = 18, minute = 0, city = "CDMX")
        val merged = ai.orElse(ocr)
        assertEquals("Overkill", merged.title)
        assertEquals(18, merged.day)
        assertEquals(18, merged.hour)
        assertEquals("CDMX", merged.city)
    }

    @Test
    fun ensureUpcoming_corrige_año_pasado_inventado() {
        val nano = FlyerData(title = "Mogwai", year = 2024, month = 3, day = 14)
        assertEquals(2027, FlyerTextParser.ensureUpcoming(nano, today).year)
        // Fecha de este año aún futura se respeta
        assertEquals(2026, FlyerTextParser.ensureUpcoming(nano.copy(year = 2026, month = 12), today).year)
        // 29 de febrero salta al siguiente año bisiesto
        assertEquals(2028, FlyerTextParser.ensureUpcoming(nano.copy(year = 2023, month = 2, day = 29), today).year)
    }

    @Test
    fun json_del_modelo_en_mayusculas_se_normaliza() {
        val data = FlyerTextParser.parseModelJson("""{"artist": "MOGWAI", "venue": "TEATRO METROPOLITAN"}""")
        assertEquals("Mogwai", data.title)
        assertEquals("Teatro Metropolitan", data.place)
    }
}
