package com.example.cuentaregresivaeventos.ui.screens

import android.content.ClipboardManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.cuentaregresivaeventos.ui.EventUi
import com.example.cuentaregresivaeventos.util.EventSearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: EventUi,
    now: () -> Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAppendNotes: (String) -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }
    val isPast = event.dateTimeMillis <= now()
    val overlayButtonColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = Color.Black.copy(alpha = 0.45f),
        contentColor = Color.White
    )

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 5f)
                        .clickable(enabled = !event.imagePath.isNullOrEmpty()) { showFullImage = true }
                ) {
                    FlyerImage(event.imagePath, event.title, Modifier.fillMaxSize(), grayscale = isPast)
                    Box(modifier = Modifier.fillMaxSize().background(FlyerScrim))
                    Text(
                        event.title,
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.padding(20.dp).navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (isPast) {
                        Text(
                            "Este evento ya pasó",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Faltan",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            CountdownBlocks(event.dateTimeMillis, now, Modifier.fillMaxWidth())
                        }
                    }
                    InfoRow(Icons.Default.DateRange, "Fecha", event.formattedDateTime)
                    event.locationText().takeIf { it.isNotEmpty() }?.let { InfoRow(Icons.Default.Place, "Lugar", it) }
                    event.description?.takeIf { it.isNotBlank() }?.let { InfoRow(Icons.Default.Info, "Notas", it) }
                    if (!isPast) SearchSection(event, onAppendNotes)
                    Spacer(Modifier.height(24.dp))
                }
            }

            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack, colors = overlayButtonColors) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    FilledTonalIconButton(onClick = onEdit, colors = overlayButtonColors) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    FilledTonalIconButton(onClick = { confirmDelete = true }, colors = overlayButtonColors) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            )
        }
    }

    if (showFullImage && !event.imagePath.isNullOrEmpty()) {
        FullScreenImageDialog(imagePath = event.imagePath, onDismiss = { showFullImage = false })
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("¿Eliminar evento?") },
            text = { Text("Se borrará \"${event.title}\" y su flyer. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchSection(event: EventUi, onAppendNotes: (String) -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var pasteMessage by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Buscar más info", style = MaterialTheme.typography.titleMedium)
        Text(
            "Lugar confirmado, precios y dónde comprar boletos. Copia lo útil y pégalo en las notas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { uriHandler.openUri(EventSearch.google(event.title, event.city, event.dateTimeMillis)) },
                label = { Text("Google") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            AssistChip(
                onClick = { uriHandler.openUri(EventSearch.tickets(event.title, event.city, event.dateTimeMillis)) },
                label = { Text("Boletos y precio") },
                leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            AssistChip(
                onClick = { uriHandler.openUri(EventSearch.ticketmaster(event.title)) },
                label = { Text("Ticketmaster") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }
        OutlinedButton(onClick = {
            val text = context.getSystemService(ClipboardManager::class.java)?.primaryClip
                ?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
            pasteMessage = if (text.isNullOrEmpty()) {
                "El portapapeles está vacío; copia primero el texto en el navegador"
            } else {
                onAppendNotes(text)
                "Agregado a las notas"
            }
        }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Pegar en notas")
        }
        pasteMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun FullScreenImageDialog(imagePath: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imagePath,
                contentDescription = "Flyer en pantalla completa",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            FilledTonalIconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                ),
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar")
            }
        }
    }
}
