package com.example.cuentaregresivaeventos.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaregresivaeventos.data.EventEntity
import com.example.cuentaregresivaeventos.data.EventRepository
import com.example.cuentaregresivaeventos.util.DateFormatter
import androidx.glance.appwidget.updateAll
import com.example.cuentaregresivaeventos.EventsWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val formattedDateTime: String // fecha y hora formateadas en español
)

class EventViewModel(
    application: Application,
    private val repository: EventRepository
) : AndroidViewModel(application) {

    // WhileSubscribed: con la app en segundo plano se detienen la consulta y el reloj
    val events: StateFlow<List<EventUi>> = repository.events
        .map { list -> list.map { it.toUi() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Reloj para la cuenta regresiva en vivo; la UI calcula el tiempo restante con este valor
    val now: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), System.currentTimeMillis())

    private fun EventEntity.toUi() = EventUi(
        id = id,
        title = title,
        place = place,
        city = city,
        description = description,
        imagePath = imagePath,
        dateTimeMillis = dateTimeMillis,
        formattedDateTime = DateFormatter.formatDateTimeSpanish(dateTimeMillis)
    )

    // Redibuja todas las instancias del widget con los datos actuales
    private suspend fun refreshWidget() {
        EventsWidget().updateAll(getApplication())
    }

    private suspend fun copyImageToInternal(uriString: String): String? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = getApplication<Application>().contentResolver
            val uri = Uri.parse(uriString)
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
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
            // Actualizar widget
            refreshWidget()
        }
    }

    fun updateEvent(eventUi: EventUi) {
        viewModelScope.launch {
            val oldImagePath = events.value.find { it.id == eventUi.id }?.imagePath
            var imagePath = eventUi.imagePath
            if (imagePath != null && !File(imagePath).exists()) {
                // It's a URI string, copy to internal; si falla la copia se conserva la imagen anterior
                imagePath = copyImageToInternal(imagePath) ?: oldImagePath
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
            if (oldImagePath != null && oldImagePath != imagePath) deleteImage(oldImagePath)
            // Actualizar widget
            refreshWidget()
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
            // Actualizar widget
            refreshWidget()
        }
    }
}
