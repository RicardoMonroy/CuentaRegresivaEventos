package com.example.cuentaregresivaeventos.flyer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Resultado de leer un flyer; `usedAi` indica si Gemini Nano aportó datos. */
data class FlyerResult(val data: FlyerData, val usedAi: Boolean)

/**
 * Lee un flyer en el teléfono, sin internet ni costo: OCR de ML Kit siempre y,
 * si AICore lo tiene, Gemini Nano. Lo de Nano manda; el OCR rellena lo que falte.
 */
class FlyerReader(private val context: Context, private val scope: CoroutineScope) {

    private val nano by lazy { Generation.getClient() }
    private var downloadingNano = false

    suspend fun read(source: String): FlyerResult? = withContext(Dispatchers.Default) {
        val bitmap = loadBitmap(source) ?: return@withContext null
        val today = LocalDate.now()
        val ocr = try {
            FlyerTextParser.parse(recognizeText(bitmap), today)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Falló el OCR del flyer", e)
            FlyerData()
        }
        val ai = readWithNano(bitmap, today)
        FlyerResult(ai?.orElse(ocr) ?: ocr, usedAi = ai != null)
    }

    private suspend fun readWithNano(bitmap: Bitmap, today: LocalDate): FlyerData? = try {
        when (nano.checkStatus()) {
            FeatureStatus.AVAILABLE -> {
                val response = nano.generateContent(
                    generateContentRequest(ImagePart(bitmap), TextPart(prompt(today))) {
                        temperature = 0.1f
                        maxOutputTokens = 256
                    }
                )
                response.candidates.firstOrNull()?.text
                    ?.let(FlyerTextParser::parseModelJson)
                    ?.let { FlyerTextParser.ensureUpcoming(it, today) }
                    ?.takeUnless { it.isEmpty }
            }
            FeatureStatus.DOWNLOADABLE -> {
                // Primera vez: se descarga el modelo en segundo plano y esta lectura usa solo OCR
                startNanoDownload()
                null
            }
            else -> null
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Gemini Nano no disponible", e)
        null
    }

    private fun startNanoDownload() {
        if (downloadingNano) return
        downloadingNano = true
        scope.launch {
            try {
                nano.download().collect { Log.d(TAG, "Descarga de Gemini Nano: $it") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo descargar Gemini Nano", e)
            } finally {
                downloadingNano = false
            }
        }
    }

    private suspend fun recognizeText(bitmap: Bitmap): List<OcrLine> = suspendCancellableCoroutine { cont ->
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { text ->
                cont.resume(text.textBlocks.flatMap { block ->
                    block.lines.map { OcrLine(it.text, it.boundingBox?.height() ?: 0) }
                })
            }
            .addOnFailureListener { cont.resumeWithException(it) }
            .addOnCompleteListener { recognizer.close() }
    }

    // shortcut: no corrige la rotación EXIF de fotos de cámara; los flyers suelen ser capturas o descargas
    private fun loadBitmap(source: String): Bitmap? = try {
        val uri = if (source.startsWith("/")) Uri.fromFile(File(source)) else Uri.parse(source)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_PX) sample *= 2
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        }
    } catch (e: Exception) {
        Log.w(TAG, "No se pudo abrir el flyer", e)
        null
    }

    private fun prompt(today: LocalDate) = """
        Eres un asistente que extrae datos de flyers de conciertos.
        Responde SOLO con un objeto JSON con estas claves:
        "artist": banda o artista principal,
        "date": fecha del concierto en formato YYYY-MM-DD,
        "time": hora de inicio en formato HH:mm de 24 horas,
        "venue": nombre del recinto,
        "city": ciudad,
        "notes": precio, preventa, boletera o teloneros en una sola línea.
        Si el flyer lista varias fechas o ciudades, usa la de México.
        Si un dato no aparece en el flyer, usa null; no inventes nada.
        Hoy es $today; si la fecha no trae año, usa la próxima vez que ocurra.
    """.trimIndent()

    private companion object {
        const val TAG = "FlyerReader"
        const val MAX_PX = 1536
    }
}
