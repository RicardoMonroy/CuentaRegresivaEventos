package com.example.cuentaregresivaeventos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import com.example.cuentaregresivaeventos.ui.EventUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownScreen(
    events: List<EventUi>,
    onAddEvent: (String, String, String, String?, String?, Long) -> Unit,
    onDeleteEvent: (EventUi) -> Unit
) {
    // Por ahora un formulario muy simple inline (luego lo refinamos)
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir evento")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = true)
            ) {
                items(events) { event ->
                    EventItem(
                        event = event,
                        onDelete = { onDeleteEvent(event) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (showAdd) {
                SimpleAddEvent(
                    onSave = { title, place, city, description, dateTimeMillis ->
                        onAddEvent(title, place, city, description, null, dateTimeMillis)
                        showAdd = false
                    },
                    onCancel = { showAdd = false }
                )
            }
        }
    }
}

@Composable
fun EventItem(
    event: EventUi,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, style = MaterialTheme.typography.titleMedium)
                Text(text = "${event.place} - ${event.city}")
                Text(text = event.remainingText)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

@Composable
fun SimpleAddEvent(
    onSave: (String, String, String, String?, Long) -> Unit,
    onCancel: () -> Unit
) {
    // Versión mínima: título fijo y fecha en +1 hora
    // Solo para probar que se inserta y se lista; luego añadimos formulario completo.
    val nowPlusHour = remember { System.currentTimeMillis() + 60 * 60 * 1000 }

    LaunchedEffect(Unit) {
        onSave("Evento de prueba", "Lugar", "Ciudad", null, nowPlusHour)
    }

    // No muestra UI; solo crea un evento de prueba automáticamente.
    // Más adelante sustituiremos esto por TextFields y DatePicker.
    onCancel()
}
