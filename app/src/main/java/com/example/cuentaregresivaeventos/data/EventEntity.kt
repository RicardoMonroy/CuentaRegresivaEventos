package com.example.cuentaregresivaeventos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val place: String,
    val city: String,
    val description: String?,
    val imageUri: String?, // por ahora solo String
    val dateTimeMillis: Long
)