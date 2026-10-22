package com.example.cuentaregresivaeventos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuentaregresivaeventos.data.EventDatabase
import com.example.cuentaregresivaeventos.data.EventRepository
import com.example.cuentaregresivaeventos.ui.EventViewModel
import com.example.cuentaregresivaeventos.ui.screens.CountdownScreen
import com.example.cuentaregresivaeventos.ui.theme.CuentaRegresivaEventosTheme

class MainActivity : ComponentActivity() {

    // Flyer recibido con "Compartir" desde otra app, pendiente de abrir en el editor
    private var sharedImage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Tras rotar la pantalla el intent sigue igual; solo se lee al crear la actividad por primera vez
        if (savedInstanceState == null) sharedImage = sharedImageOf(intent)

        // Crear DB y repositorio una sola vez
        val db = EventDatabase.getInstance(applicationContext)
        val repository = EventRepository(db.eventDao())

        setContent {
            CuentaRegresivaEventosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CountdownApp(
                        repository = repository,
                        application = application,
                        sharedImage = sharedImage,
                        onSharedImageHandled = { sharedImage = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        sharedImageOf(intent)?.let { sharedImage = it }
    }

    private fun sharedImageOf(intent: Intent?): String? =
        intent?.takeIf { it.action == Intent.ACTION_SEND && it.type?.startsWith("image/") == true }
            ?.let { IntentCompat.getParcelableExtra(it, Intent.EXTRA_STREAM, Uri::class.java) }
            ?.toString()
}

@Composable
fun CountdownApp(
    repository: EventRepository,
    application: android.app.Application,
    sharedImage: String?,
    onSharedImageHandled: () -> Unit
) {
    // Factory para el ViewModel con dependencia repository y application
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(application, repository) as T
        }
    }

    val viewModel: EventViewModel = viewModel(factory = factory)
    // WithLifecycle: deja de escuchar (y de correr el reloj) cuando la app no está visible
    val eventsState by viewModel.events.collectAsStateWithLifecycle()
    val nowState = viewModel.now.collectAsStateWithLifecycle()

    CountdownScreen(
        events = eventsState,
        now = { nowState.value },
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
        },
        onReadFlyer = viewModel::readFlyer,
        sharedImage = sharedImage,
        onSharedImageHandled = onSharedImageHandled
    )
}
