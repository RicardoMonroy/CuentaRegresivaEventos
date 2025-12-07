package com.example.cuentaregresivaeventos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuentaregresivaeventos.data.EventDatabase
import com.example.cuentaregresivaeventos.data.EventRepository
import com.example.cuentaregresivaeventos.ui.EventViewModel
import com.example.cuentaregresivaeventos.ui.screens.CountdownScreen
import com.example.cuentaregresivaeventos.ui.theme.CuentaRegresivaEventosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Crear DB y repositorio una sola vez
        val db = EventDatabase.getInstance(applicationContext)
        val repository = EventRepository(db.eventDao())

        setContent {
            CuentaRegresivaEventosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CountdownApp(repository = repository)
                }
            }
        }
    }
}

@Composable
fun CountdownApp(repository: EventRepository) {
    // Factory para el ViewModel con dependencia repository
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(repository) as T
        }
    }

    val viewModel: EventViewModel = viewModel(factory = factory)
    val eventsState by viewModel.events.collectAsState()

    CountdownScreen(
        events = eventsState,
        onAddEvent = { title, place, city, description, imageUri, dateTimeMillis ->
            viewModel.addEvent(
                title = title,
                place = place,
                city = city,
                description = description,
                imageUri = imageUri,
                dateTimeMillis = dateTimeMillis
            )
        },
        onDeleteEvent = { eventUi ->
            viewModel.deleteEvent(eventUi)
        },
        onUpdateEvent = { updatedUi ->
            viewModel.updateEvent(updatedUi)
        }
    )
}
