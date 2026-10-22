package com.example.cuentaregresivaeventos.flyer

import java.text.Normalizer
import java.time.LocalDate

/** Una línea de texto reconocida en el flyer; `height` (px) sirve para hallar el texto más grande. */
data class OcrLine(val text: String, val height: Int)

/** Datos extraídos de un flyer; cualquier campo puede faltar. `month` va de 1 a 12. */
data class FlyerData(
    val title: String? = null,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val place: String? = null,
    val city: String? = null,
    val notes: String? = null
) {
    val hasDate: Boolean get() = year != null && month != null && day != null
    val isEmpty: Boolean get() = title == null && !hasDate && hour == null && place == null && city == null && notes == null

    /** Completa lo que falta con [other]; fecha y hora se toman en bloque para no mezclar fuentes. */
    fun orElse(other: FlyerData): FlyerData {
        val dateSource = if (hasDate) this else other
        val timeSource = if (hour != null) this else other
        return FlyerData(
            title = title ?: other.title,
            year = dateSource.year,
            month = dateSource.month,
            day = dateSource.day,
            hour = timeSource.hour,
            minute = timeSource.minute,
            place = place ?: other.place,
            city = city ?: other.city,
            notes = notes ?: other.notes
        )
    }
}

/** Reglas para sacar datos del texto de un flyer de concierto (español e inglés). */
object FlyerTextParser {

    private val MONTHS = mapOf(
        "ENERO" to 1, "ENE" to 1, "JANUARY" to 1, "JAN" to 1,
        "FEBRERO" to 2, "FEBRUARY" to 2, "FEB" to 2,
        "MARZO" to 3, "MARCH" to 3, "MAR" to 3,
        "ABRIL" to 4, "APRIL" to 4, "ABR" to 4, "APR" to 4,
        "MAYO" to 5, "MAY" to 5,
        "JUNIO" to 6, "JUNE" to 6, "JUN" to 6,
        "JULIO" to 7, "JULY" to 7, "JUL" to 7,
        "AGOSTO" to 8, "AUGUST" to 8, "AGO" to 8, "AUG" to 8,
        "SEPTIEMBRE" to 9, "SETIEMBRE" to 9, "SEPTEMBER" to 9, "SEPT" to 9, "SEP" to 9,
        "OCTUBRE" to 10, "OCTOBER" to 10, "OCT" to 10,
        "NOVIEMBRE" to 11, "NOVEMBER" to 11, "NOV" to 11,
        "DICIEMBRE" to 12, "DECEMBER" to 12, "DIC" to 12, "DEC" to 12
    )
    private val MONTH = MONTHS.keys.joinToString("|")

    // "18 DE MARZO DE 2027", "15 NOV"
    private val DAY_MONTH = Regex("""\b(\d{1,2})\s*(?:DE\s+)?($MONTH)\b\.?(?:\s*(?:DEL?\s+)?(20\d{2}))?""")
    // "MAR 18", "MARCH 18, 2027"
    private val MONTH_DAY = Regex("""\b($MONTH)\b\.?\s*(\d{1,2})\b(?:\s*,?\s*(20\d{2}))?""")
    // "18/03/2027", "18-03-27"
    private val NUMERIC_DATE = Regex("""\b(\d{1,2})[/.-](\d{1,2})[/.-](\d{4}|\d{2})\b""")
    private val YEAR = Regex("""\b(20\d{2})\b""")

    private val TIME_24 = Regex("""\b([01]?\d|2[0-3]):([0-5]\d)\b""")
    private val TIME_12 = Regex("""\b(1[0-2]|0?[1-9])(?::([0-5]\d))?\s*([AP])\.?\s?M\b""")
    private val TIME_HRS = Regex("""\b([01]?\d|2[0-3])\s*(?:HRS|HR|H)\b""")
    private val SHOW_LINE = Regex("""\b(SHOW|INICIO|CONCIERTO|EVENTO)\b""")

    private val MEXICO = Regex("""\b(MEXICO|CDMX|MX)\b""")
    private val VENUE = Regex(
        """\b(FORO|MULTIFORO|ARENA|AUDITORIO|TEATRO|ESTADIO|PALACIO|PEPSI CENTER|PLAZA|SALON|CLUB|BAR|DOMO|""" +
            """VELODROMO|AUTODROMO|HIPODROMO|FRONTON|PARQUE|SALA|EXPLANADA|CENTRO|HALL|CAFE|LUNARIO|CIRCO VOLADOR|TEMPLO)\b"""
    )
    // Ciudades específicas primero; "MEXICO" a secas al final se toma como CDMX
    private val CITIES = linkedMapOf(
        "CIUDAD DE MEXICO" to "CDMX", "CDMX" to "CDMX", "MONTERREY" to "Monterrey", "GUADALAJARA" to "Guadalajara",
        "PUEBLA" to "Puebla", "QUERETARO" to "Querétaro", "TIJUANA" to "Tijuana", "LEON" to "León",
        "MERIDA" to "Mérida", "CANCUN" to "Cancún", "TOLUCA" to "Toluca", "AGUASCALIENTES" to "Aguascalientes",
        "SAN LUIS POTOSI" to "San Luis Potosí", "MORELIA" to "Morelia", "OAXACA" to "Oaxaca",
        "VERACRUZ" to "Veracruz", "CHIHUAHUA" to "Chihuahua", "HERMOSILLO" to "Hermosillo",
        "CULIACAN" to "Culiacán", "SALTILLO" to "Saltillo", "TORREON" to "Torreón", "MEXICALI" to "Mexicali",
        "MEXICO" to "CDMX"
    )
    private val NOT_TITLE = Regex(
        """\b(PRESENTA|PRESENTAN|BOLETOS|TICKETS|TOUR|PREVENTA|PUERTAS|DOORS|INVITADOS?|ESPECIAL|SOLD OUT|AGOTADO|WWW|COM|CONCIERTOS?)\b"""
    )
    private val NOTE = Regex(
        """(\$\s?\d|\b(BOLETOS|TICKETMASTER|ETICKET|BOLETIA|FUNTICKET|PASSLINE|PREVENTA|TAQUILLA|COVER|ENTRADA)\b)"""
    )

    private data class DateHit(val line: Int, val month: Int, val day: Int, val year: Int?)

    fun parse(lines: List<OcrLine>, today: LocalDate): FlyerData {
        val clean = lines.filter { it.text.isNotBlank() }
        val norm = clean.map { normalize(it.text) }

        val date = findDate(norm, today)
        val time = findTime(norm)
        val venueIndex = norm.indexOfFirst { VENUE.containsMatchIn(it) && it.length <= 45 }
        val city = CITIES.entries.firstOrNull { (key, _) -> norm.any { Regex("""\b$key\b""").containsMatchIn(it) } }?.value
        val notes = clean.indices.filter { NOTE.containsMatchIn(norm[it]) }
            .take(3).joinToString(" · ") { clean[it].text.trim() }.ifEmpty { null }
        val title = clean.indices
            .filter { i ->
                val line = norm[i]
                line.count { it.isLetter() } >= 3 && i != venueIndex &&
                    !NOT_TITLE.containsMatchIn(line) && !looksLikeDateOrTime(line) &&
                    CITIES.keys.none { line == it }
            }
            .maxByOrNull { clean[it].height }
            ?.let { prettify(clean[it].text) }

        return FlyerData(
            title = title,
            year = date?.year,
            month = date?.monthValue,
            day = date?.dayOfMonth,
            hour = time?.first,
            minute = time?.second,
            place = venueIndex.takeIf { it >= 0 }?.let { venueName(clean[it].text) },
            city = city,
            notes = notes
        )
    }

    /** Lee el JSON que devuelve Gemini Nano; tolera texto alrededor y valores null. */
    fun parseModelJson(text: String): FlyerData {
        fun field(key: String): String? =
            Regex(""""$key"\s*:\s*"([^"]*)"""").find(text)?.groupValues?.get(1)?.trim()
                ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

        val date = field("date")?.let { Regex("""(\d{4})-(\d{1,2})-(\d{1,2})""").find(it) }?.let {
            runCatching { LocalDate.of(it.groupValues[1].toInt(), it.groupValues[2].toInt(), it.groupValues[3].toInt()) }.getOrNull()
        }
        val time = field("time")?.let { Regex("""(\d{1,2}):(\d{2})""").find(it) }
            ?.let { it.groupValues[1].toInt() to it.groupValues[2].toInt() }
            ?.takeIf { (h, m) -> h in 0..23 && m in 0..59 }

        return FlyerData(
            title = field("artist")?.let(::prettify),
            year = date?.year,
            month = date?.monthValue,
            day = date?.dayOfMonth,
            hour = time?.first,
            minute = time?.second,
            place = field("venue")?.let(::prettify),
            city = field("city"),
            notes = field("notes")
        )
    }

    /**
     * Un flyer anuncia algo por venir: si la fecha leída ya pasó (Gemini Nano a veces inventa el año
     * cuando el flyer no lo trae), se mueve a la próxima vez que caiga ese día.
     */
    fun ensureUpcoming(data: FlyerData, today: LocalDate): FlyerData {
        if (!data.hasDate) return data
        var year = data.year!!
        while (year <= today.year + 1) {
            val date = runCatching { LocalDate.of(year, data.month!!, data.day!!) }.getOrNull()
            if (date != null && !date.isBefore(today)) break
            year++
        }
        return data.copy(year = year)
    }

    private fun findDate(norm: List<String>, today: LocalDate): LocalDate? {
        val hits = norm.flatMapIndexed { i, line ->
            DAY_MONTH.findAll(line).map { DateHit(i, MONTHS.getValue(it.groupValues[2]), it.groupValues[1].toInt(), it.groupValues[3].toIntOrNull()) } +
                MONTH_DAY.findAll(line).map { DateHit(i, MONTHS.getValue(it.groupValues[1]), it.groupValues[2].toInt(), it.groupValues[3].toIntOrNull()) } +
                NUMERIC_DATE.findAll(line).map {
                    val y = it.groupValues[3].toInt()
                    DateHit(i, it.groupValues[2].toInt(), it.groupValues[1].toInt(), if (y < 100) 2000 + y else y)
                }
        }.filter { it.month in 1..12 && it.day in 1..31 }
        if (hits.isEmpty()) return null

        // Giras con varias fechas: la de México (misma línea, la de abajo o la de arriba)
        // shortcut: país preferido fijo en México, parametrizar si algún día se usa fuera
        val mexicoLines = norm.indices.filter { MEXICO.containsMatchIn(norm[it]) }
        val hit = mexicoLines.firstNotNullOfOrNull { m ->
            hits.firstOrNull { it.line == m } ?: hits.firstOrNull { it.line == m + 1 } ?: hits.firstOrNull { it.line == m - 1 }
        } ?: hits.first()

        val year = hit.year ?: inferYear(hit, norm, today)
        return runCatching { LocalDate.of(year, hit.month, hit.day) }.getOrNull()
    }

    // Un año suelto en el flyer ("LATIN AMERICA 2027") manda; si no, la próxima vez que caiga esa fecha
    private fun inferYear(hit: DateHit, norm: List<String>, today: LocalDate): Int {
        val loose = norm.flatMap { line -> YEAR.findAll(line).map { it.value.toInt() } }.filter { it >= today.year }.minOrNull()
        if (loose != null && runCatching { LocalDate.of(loose, hit.month, hit.day) }.getOrNull()?.isBefore(today) == false) {
            return loose
        }
        val thisYear = runCatching { LocalDate.of(today.year, hit.month, hit.day) }.getOrNull()
        return if (thisYear != null && !thisYear.isBefore(today)) today.year else today.year + 1
    }

    // La hora del show manda sobre la de puertas
    private fun findTime(norm: List<String>): Pair<Int, Int>? {
        val ordered = norm.sortedByDescending { SHOW_LINE.containsMatchIn(it) }
        for (fullLine in ordered) {
            // En "PUERTAS 17:00 SHOW 18:00" se busca a partir de "SHOW"
            val line = fullLine.substring(SHOW_LINE.find(fullLine)?.range?.first ?: 0)
            // AM/PM antes que 24 h: "9:30 PM" también encaja en el patrón de 24 h
            TIME_12.find(line)?.let {
                val hour = it.groupValues[1].toInt() % 12 + if (it.groupValues[3] == "P") 12 else 0
                return hour to (it.groupValues[2].toIntOrNull() ?: 0)
            }
            TIME_24.find(line)?.let { return it.groupValues[1].toInt() to it.groupValues[2].toInt() }
            TIME_HRS.find(line)?.let { return it.groupValues[1].toInt() to 0 }
        }
        return null
    }

    // "FORO 28 · CDMX" -> "Foro 28": la ciudad va en su propio campo
    private fun venueName(text: String): String =
        text.split(Regex("""\s*[·|,]\s*""")).filter { part -> CITIES.keys.none { normalize(part) == it } }
            .joinToString(", ").let(::prettify)

    private fun looksLikeDateOrTime(line: String) =
        DAY_MONTH.containsMatchIn(line) || MONTH_DAY.containsMatchIn(line) || NUMERIC_DATE.containsMatchIn(line) ||
            TIME_24.containsMatchIn(line) || TIME_12.containsMatchIn(line)

    private fun normalize(text: String): String =
        Normalizer.normalize(text.uppercase(), Normalizer.Form.NFD).replace(Regex("""\p{Mn}+"""), "").trim()

    // "FORO 28" -> "Foro 28"; texto que ya trae minúsculas se respeta
    private fun prettify(text: String): String {
        val trimmed = text.trim().trim('@', '-', '·', '|', ':').trim()
        if (trimmed.any { it.isLowerCase() }) return trimmed
        return trimmed.split(Regex("""\s+""")).joinToString(" ") { word ->
            if (word.any { it.isDigit() || it == '/' }) word else word.lowercase().replaceFirstChar { it.titlecase() }
        }
    }
}
