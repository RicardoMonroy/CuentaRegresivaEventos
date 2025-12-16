package com.example.cuentaregresivaeventos.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaregresivaeventos.data.EventEntity
import com.example.cuentaregresivaeventos.data.EventRepository
import com.example.cuentaregresivaeventos.util.DateFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

data class EventUi(
    val id: Long,
    val title: String,
    val place: String,
    val city: String,
    val description: String?,
    val imagePath: String?,
    val dateTimeMillis: Long,
    val remainingText: String, // texto ya calculado para mostrar
    val formattedDate: String, // fecha formateada en español
    val formattedDateTime: String // fecha y hora formateadas en español
)

class EventViewModel(
    application: Application,
    private val repository: EventRepository
) : AndroidViewModel(application) {

    private val _events = MutableStateFlow<List<EventUi>>(emptyList())
    val events: StateFlow<List<EventUi>> = _events.asStateFlow()

    private var lastRawEvents: List<EventEntity> = emptyList()

    init {
        // Observar base de datos
        viewModelScope.launch {
            repository.events.collect { list ->
                lastRawEvents = list
                recalculateRemaining()
            }
        }

        // Ticker para actualizar countdown cada segundo
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                recalculateRemaining()
            }
        }
    }

    private fun recalculateRemaining() {
        val now = System.currentTimeMillis()
        _events.value = lastRawEvents.map { entity ->
            val diff = entity.dateTimeMillis - now
            val remaining = if (diff <= 0) {
                "Evento pasado"
            } else {
                DateFormatter.formatCountdownSpanish(entity.dateTimeMillis)
            }
            EventUi(
                id = entity.id,
                title = entity.title,
                place = entity.place,
                city = entity.city,
                description = entity.description,
                imagePath = entity.imagePath,
                dateTimeMillis = entity.dateTimeMillis,
                remainingText = remaining,
                formattedDate = DateFormatter.formatDateSpanish(entity.dateTimeMillis),
                formattedDateTime = DateFormatter.formatDateTimeSpanish(entity.dateTimeMillis)
            )
        }
    }


    private fun copyImageToInternal(uriString: String): String? {
        return try {
            val contentResolver = getApplication<Application>().contentResolver
            val uri = Uri.parse(uriString)
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = "event_image_${System.currentTimeMillis()}.jpg"
            val imagesDir = File(getApplication<Application>().filesDir, "images")
            imagesDir.mkdirs()
            val file = File(imagesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun deleteImage(path: String) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addEvent(
        title: String,
        place: String,
        city: String,
        description: String?,
        imageUri: String?,
        dateTimeMillis: Long
    ) {
        viewModelScope.launch {
            val imagePath = imageUri?.let { copyImageToInternal(it) }
            repository.addEvent(
                EventEntity(
                    title = title,
                    place = place,
                    city = city,
                    description = description,
                    imagePath = imagePath,
                    dateTimeMillis = dateTimeMillis
                )
            )
        }
    }

    fun updateEvent(eventUi: EventUi) {
        viewModelScope.launch {
            var imagePath = eventUi.imagePath
            if (imagePath != null && !File(imagePath).exists()) {
                // It's a URI string, copy to internal
                imagePath = copyImageToInternal(imagePath)
            }
            repository.updateEvent(
                EventEntity(
                    id = eventUi.id,
                    title = eventUi.title,
                    place = eventUi.place,
                    city = eventUi.city,
                    description = eventUi.description,
                    imagePath = imagePath,
                    dateTimeMillis = eventUi.dateTimeMillis
                )
            )
        }
    }

    fun deleteEvent(eventUi: EventUi) {
        viewModelScope.launch {
            val entity = EventEntity(
                id = eventUi.id,
                title = eventUi.title,
                place = eventUi.place,
                city = eventUi.city,
                description = eventUi.description,
                imagePath = eventUi.imagePath,
                dateTimeMillis = eventUi.dateTimeMillis
            )
            repository.deleteEvent(entity)
            eventUi.imagePath?.let { deleteImage(it) }
        }
    }
}
