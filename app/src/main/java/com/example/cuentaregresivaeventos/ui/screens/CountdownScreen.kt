package com.example.cuentaregresivaeventos.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
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
import android.content.Context
import com.example.cuentaregresivaeventos.util.DateFormatter


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

            val context = LocalContext.current

            if (showAddDialog) {
                AddEventDialog(
                    context = context,
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
            if (!event.imagePath.isNullOrEmpty()) {
                AsyncImage(
                    model = event.imagePath,
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
                    Text(
                        text = "Fecha: " + event.formattedDateTime,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
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
    context: Context,
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
            if (uri != null) {
                imageUri = uri
            }
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
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentScale = ContentScale.Crop
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
    var showFullImage by remember { mutableStateOf(false) }

    // Image full screen dialog
    if (showFullImage && !event.imagePath.isNullOrEmpty()) {
        FullScreenImageDialog(
            imagePath = event.imagePath!!,
            onDismiss = { showFullImage = false }
        )
    }

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
        title = {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Image section
                if (!event.imagePath.isNullOrEmpty()) {
                    AsyncImage(
                        model = event.imagePath,
                        contentDescription = "Imagen del evento",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable { showFullImage = true }
                            .shadow(4.dp, shape = MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                    
                    Text(
                        text = "Toca la imagen para ver en pantalla completa",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${event.place} - ${event.city}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Date and time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.formattedDateTime,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                // Countdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.remainingText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (event.remainingText.contains("pasado", ignoreCase = true))
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
                
                // Description
                if (!event.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Descripción:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = event.description!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDelete) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }
        },
        dismissButton = {
            Button(
                onClick = { showEditDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Editar")
            }
        }
    )
}

@Composable
fun FullScreenImageDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }
                .background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imagePath,
                contentDescription = "Imagen en pantalla completa",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
            
            // Close button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Instructions text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Toca cualquier lugar para cerrar",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
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
            DateFormatter.formatDateSpanish(event.dateTimeMillis)
        )
    }
    var timeText by remember {
        mutableStateOf(
            DateFormatter.formatTimeForWidget(event.dateTimeMillis)
        )
    }
    var selectedDateTimeMillis by remember { mutableStateOf(event.dateTimeMillis) }

    var imagePath by remember { mutableStateOf(event.imagePath) }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            imagePath = uri?.toString()
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
                                imagePath = imagePath,
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
                        if (imagePath == null)
                            "Seleccionar imagen (flyer)"
                        else
                            "Cambiar imagen seleccionada"
                    )
                }

                if (imagePath != null) {
                    AsyncImage(
                        model = imagePath,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentScale = ContentScale.Crop
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
