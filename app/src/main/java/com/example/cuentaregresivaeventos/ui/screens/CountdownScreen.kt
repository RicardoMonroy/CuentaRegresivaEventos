package com.example.cuentaregresivaeventos.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.cuentaregresivaeventos.ui.EventUi
import java.util.Calendar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Edit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownScreen(
    events: List<EventUi>,
    onAddEvent: (String, String, String, String?, String?, Long) -> Unit,
    onDeleteEvent: (EventUi) -> Unit,
    onUpdateEvent: (EventUi) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos (cuenta regresiva)") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir evento")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (events.isEmpty()) {
                Text(
                    text = "No hay eventos, pulsa + para agregar uno",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(events) { event ->
                        EventItem(
                            event = event,
                            onDelete = { onDeleteEvent(event) },
                            onUpdate = { onUpdateEvent(it) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (showAddDialog) {
                AddEventDialog(
                    onSave = { title, place, city, description, dateTimeMillis, imageUri ->
                        onAddEvent(title, place, city, description, imageUri, dateTimeMillis)
                        showAddDialog = false
                    },
                    onDismiss = { showAddDialog = false }
                )
            }
        }
    }
}

@Composable
fun EventItem(
    event: EventUi,
    onDelete: () -> Unit,
    onUpdate: (EventUi) -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { showDetails = true }
    ) {
        Box {
            if (!event.imageUri.isNullOrEmpty()) {
                AsyncImage(
                    model = event.imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
            }

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "${event.place} - ${event.city}",
                        color = Color.White
                    )
                    Text(
                        text = event.remainingText,
                        color = Color.White
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.White
                    )
                }
            }
        }
    }

    if (showDetails) {
        EventDetailsDialog(
            event = event,
            onDismiss = { showDetails = false },
            onDelete = {
                onDelete()
                showDetails = false
            },
            onEdit = { updated ->
                onUpdate(updated)
                showDetails = false
            }
        )
    }
}


@Composable
fun AddEventDialog(
    onSave: (String, String, String, String?, Long, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var place by remember { mutableStateOf(TextFieldValue("")) }
    var city by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }

    val calendar = remember { Calendar.getInstance() }
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    var selectedDateTimeMillis by remember { mutableStateOf<Long?>(null) }

    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            imageUri = uri
        }

    fun openDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)
                dateText = "%02d/%02d/%04d".format(d, m + 1, y)
                if (timeText.isNotEmpty()) {
                    selectedDateTimeMillis = calendar.timeInMillis
                }
            },
            year, month, day
        ).show()
    }

    fun openTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, min)
                calendar.set(Calendar.SECOND, 0)
                timeText = "%02d:%02d".format(h, min)
                if (dateText.isNotEmpty()) {
                    selectedDateTimeMillis = calendar.timeInMillis
                }
            },
            hour, minute, true
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = selectedDateTimeMillis
                    if (title.text.isNotBlank() &&
                        place.text.isNotBlank() &&
                        city.text.isNotBlank() &&
                        millis != null
                    ) {
                        onSave(
                            title.text.trim(),
                            place.text.trim(),
                            city.text.trim(),
                            description.text.takeIf { it.isNotBlank() }?.trim(),
                            millis,
                            imageUri?.toString()
                        )
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        title = { Text("Nuevo evento") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    label = { Text("Lugar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Ciudad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Text(
                        if (imageUri == null)
                            "Seleccionar imagen (flyer)"
                        else
                            "Cambiar imagen seleccionada"
                    )
                }

                if (imageUri != null) {
                    Text(
                        text = "Imagen seleccionada",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { openDatePicker() }) {
                        Text(if (dateText.isEmpty()) "Seleccionar fecha" else dateText)
                    }
                    Button(onClick = { openTimePicker() }) {
                        Text(if (timeText.isEmpty()) "Seleccionar hora" else timeText)
                    }
                }
            }
        }
    )
}

@Composable
fun EventDetailsDialog(
    event: EventUi,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (EventUi) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditEventDialog(
            event = event,
            onSave = { updated ->
                onEdit(updated)
            },
            onDismiss = { showEditDialog = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        dismissButton = {
            TextButton(onClick = onDelete) { Text("Eliminar") }
        },
        icon = {
            IconButton(onClick = { showEditDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar"
                )
            }
        },
        title = { Text(event.title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!event.imageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = event.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Text("${event.place} - ${event.city}")
                Text(
                    "Fecha/hora: " +
                            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                                .format(java.util.Date(event.dateTimeMillis))
                )
                Text("Cuenta regresiva: ${event.remainingText}")
                if (!event.description.isNullOrEmpty()) {
                    Text("Descripción:")
                    Text(event.description!!)
                }
            }
        }
    )
}

@Composable
fun EditEventDialog(
    event: EventUi,
    onSave: (EventUi) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(TextFieldValue(event.title)) }
    var place by remember { mutableStateOf(TextFieldValue(event.place)) }
    var city by remember { mutableStateOf(TextFieldValue(event.city)) }
    var description by remember { mutableStateOf(TextFieldValue(event.description ?: "")) }

    val calendar = remember { Calendar.getInstance().apply { timeInMillis = event.dateTimeMillis } }
    var dateText by remember {
        mutableStateOf(
            java.text.SimpleDateFormat("dd/MM/yyyy")
                .format(java.util.Date(event.dateTimeMillis))
        )
    }
    var timeText by remember {
        mutableStateOf(
            java.text.SimpleDateFormat("HH:mm")
                .format(java.util.Date(event.dateTimeMillis))
        )
    }
    var selectedDateTimeMillis by remember { mutableStateOf(event.dateTimeMillis) }

    var imageUri by remember { mutableStateOf(event.imageUri?.let { android.net.Uri.parse(it) }) }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            imageUri = uri
        }

    fun openDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)
                dateText = "%02d/%02d/%04d".format(d, m + 1, y)
                selectedDateTimeMillis = calendar.timeInMillis
            },
            year, month, day
        ).show()
    }

    fun openTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, min)
                calendar.set(Calendar.SECOND, 0)
                timeText = "%02d:%02d".format(h, min)
                selectedDateTimeMillis = calendar.timeInMillis
            },
            hour, minute, true
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.text.isNotBlank() &&
                        place.text.isNotBlank() &&
                        city.text.isNotBlank()
                    ) {
                        onSave(
                            event.copy(
                                title = title.text.trim(),
                                place = place.text.trim(),
                                city = city.text.trim(),
                                description = description.text
                                    .takeIf { it.isNotBlank() }
                                    ?.trim(),
                                imageUri = imageUri?.toString(),
                                dateTimeMillis = selectedDateTimeMillis
                            )
                        )
                    }
                }
            ) { Text("Guardar cambios") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Editar evento") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    label = { Text("Lugar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Ciudad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text(
                        if (imageUri == null)
                            "Seleccionar imagen (flyer)"
                        else
                            "Cambiar imagen seleccionada"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { openDatePicker() }) {
                        Text(dateText)
                    }
                    Button(onClick = { openTimePicker() }) {
                        Text(timeText)
                    }
                }
            }
        }
    )
}
