package com.example.cuentaregresivaeventos.data

import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val dao: EventDao
) {
    val events: Flow<List<EventEntity>> = dao.getAllEvents()

    suspend fun addEvent(event: EventEntity) {
        dao.insertEvent(event)
    }

    suspend fun updateEvent(event: EventEntity) {
        dao.updateEvent(event)
    }

    suspend fun deleteEvent(event: EventEntity) {
        dao.deleteEvent(event)
    }
}