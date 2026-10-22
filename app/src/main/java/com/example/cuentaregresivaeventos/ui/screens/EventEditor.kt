package com.example.cuentaregresivaeventos.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.cuentaregresivaeventos.ui.EventUi
import com.example.cuentaregresivaeventos.util.DateFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Editor a pantalla completa para crear (`initial == null`) o editar un evento.
 * `image` es la ruta interna del flyer actual o el content:// recién elegido; el ViewModel lo copia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditor(
    initial: EventUi?,
    onDismiss: () -> Unit,
    onSave: (title: String, place: String, city: String, description: String?, image: String?, dateTimeMillis: Long) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var place by rememberSaveable { mutableStateOf(initial?.place ?: "") }
    var city by rememberSaveable { mutableStateOf(initial?.city ?: "") }
    var description by rememberSaveable { mutableStateOf(initial?.description ?: "") }
    var image by rememberSaveable { mutableStateOf(initial?.imagePath) }
    // DatePicker de Material trabaja en medianoche UTC; la hora se guarda aparte
    var dateUtc by rememberSaveable { mutableStateOf(initial?.dateTimeMillis?.let { localToUtcDate(it) }) }
    var hour by rememberSaveable { mutableStateOf(initial?.dateTimeMillis?.let { calendarOf(it).get(Calendar.HOUR_OF_DAY) }) }
    var minute by rememberSaveable { mutableStateOf(initial?.dateTimeMillis?.let { calendarOf(it).get(Calendar.MINUTE) }) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    val flyerPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) image = uri.toString()
    }
    fun pickFlyer() = flyerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    fun save() {
        val date = dateUtc
        val h = hour
        val m = minute
        if (title.isBlank() || date == null || h == null || m == null) {
            showErrors = true
            return
        }
        onSave(
            title.trim(),
            place.trim(),
            city.trim(),
            description.trim().ifEmpty { null },
            image,
            utcDateToLocal(date, h, m)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (initial == null) "Nuevo evento" else "Editar evento") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cancelar") }
                    },
                    actions = { TextButton(onClick = ::save) { Text("Guardar") } }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FlyerPickerBox(
                    image = image,
                    onPick = ::pickFlyer,
                    onRemove = { image = null }
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Banda o evento *") },
                    singleLine = true,
                    isError = showErrors && title.isBlank(),
                    supportingText = if (showErrors && title.isBlank()) ({ Text("Escribe un título") }) else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PickerField(
                        label = "Fecha *",
                        value = dateUtc?.let { DateFormatter.formatDateSpanish(utcDateToLocal(it, 0, 0)) },
                        isError = showErrors && dateUtc == null,
                        onClick = { showDatePicker = true }
                    )
                    PickerField(
                        label = "Hora *",
                        value = if (hour != null && minute != null) String.format(Locale.ROOT, "%02d:%02d", hour, minute) else null,
                        isError = showErrors && hour == null,
                        onClick = { showTimePicker = true },
                        weight = 0.6f
                    )
                }

                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    label = { Text("Lugar") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Ciudad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notas (precio, boletos, acompañantes…)") },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateUtc ?: localToUtcDate(System.currentTimeMillis()))
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateUtc = state.selectedDateMillis ?: dateUtc
                    showDatePicker = false
                    // Flujo natural: tras elegir fecha, pedir la hora si falta
                    if (hour == null) showTimePicker = true
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(initialHour = hour ?: 20, initialMinute = minute ?: 0, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hour = state.hour
                    minute = state.minute
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
            text = { TimePicker(state = state) }
        )
    }
}

@Composable
private fun FlyerPickerBox(image: String?, onPick: () -> Unit, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(if (image != null) 4f / 5f else 16f / 9f)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onPick),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            AsyncImage(
                model = image,
                contentDescription = "Flyer seleccionado",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Row(
                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Quitar flyer")
                }
                FilledTonalButton(onClick = onPick) { Text("Cambiar flyer") }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Text("Agregar flyer", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Opcional",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RowScope.PickerField(
    label: String,
    value: String?,
    isError: Boolean,
    onClick: () -> Unit,
    weight: Float = 1f
) {
    val color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.weight(weight)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (isError) color else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value ?: "Elegir",
                style = MaterialTheme.typography.bodyLarge,
                color = if (value == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

private fun calendarOf(millis: Long): Calendar = Calendar.getInstance().apply { timeInMillis = millis }

// Fecha local -> medianoche UTC del mismo día (formato que espera DatePicker)
private fun localToUtcDate(localMillis: Long): Long {
    val local = calendarOf(localMillis)
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

// Medianoche UTC de DatePicker + hora local -> instante local real
private fun utcDateToLocal(utcDate: Long, hour: Int, minute: Int): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcDate }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), hour, minute)
    }.timeInMillis
}
