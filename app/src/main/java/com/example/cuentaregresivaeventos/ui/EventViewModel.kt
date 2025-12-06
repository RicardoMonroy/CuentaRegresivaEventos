package com.example.cuentaregresivaeventos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaregresivaeventos.data.EventEntity
import com.example.cuentaregresivaeventos.data.EventRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EventUi(
    val id: Long,
    val title: String,
    val place: String,
    val city: String,
    val description: String?,
    val imageUri: String?,
    val dateTimeMillis: Long,
    val remainingText: String // texto ya calculado para mostrar
)

class EventViewModel(
    private val repository: EventRepository
) : ViewModel() {

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
                millisToCountdown(diff)
            }
            EventUi(
                id = entity.id,
                title = entity.title,
                place = entity.place,
                city = entity.city,
                description = entity.description,
                imageUri = entity.imageUri,
                dateTimeMillis = entity.dateTimeMillis,
                remainingText = remaining
            )
        }
    }

    private fun millisToCountdown(millis: Long): String {
        val totalSeconds = millis / 1000
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return "${days}d ${hours}h ${minutes}m ${seconds}s"
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
            repository.addEvent(
                EventEntity(
                    title = title,
                    place = place,
                    city = city,
                    description = description,
                    imageUri = imageUri,
                    dateTimeMillis = dateTimeMillis
                )
            )
        }
    }

    fun updateEvent(eventUi: EventUi) {
        viewModelScope.launch {
            repository.updateEvent(
                EventEntity(
                    id = eventUi.id,
                    title = eventUi.title,
                    place = eventUi.place,
                    city = eventUi.city,
                    description = eventUi.description,
                    imageUri = eventUi.imageUri,
                    dateTimeMillis = eventUi.dateTimeMillis
                )
            )
        }
    }

    fun deleteEvent(eventUi: EventUi) {
        viewModelScope.launch {
            repository.deleteEvent(
                EventEntity(
                    id = eventUi.id,
                    title = eventUi.title,
                    place = eventUi.place,
                    city = eventUi.city,
                    description = eventUi.description,
                    imageUri = eventUi.imageUri,
                    dateTimeMillis = eventUi.dateTimeMillis
                )
            )
        }
    }
}
