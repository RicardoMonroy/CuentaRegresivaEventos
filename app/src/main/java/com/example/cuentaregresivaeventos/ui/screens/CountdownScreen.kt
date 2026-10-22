package com.example.cuentaregresivaeventos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuentaregresivaeventos.flyer.FlyerResult
import com.example.cuentaregresivaeventos.ui.EventUi
import com.example.cuentaregresivaeventos.util.DateFormatter

@Composable
fun CountdownScreen(
    events: List<EventUi>,
    now: () -> Long,
    onAddEvent: (String, String, String, String?, String?, Long) -> Unit,
    onDeleteEvent: (EventUi) -> Unit,
    onUpdateEvent: (EventUi) -> Unit,
    onReadFlyer: suspend (String) -> FlyerResult?,
    sharedImage: String? = null,
    onSharedImageHandled: () -> Unit = {}
) {
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editorImage by rememberSaveable { mutableStateOf<String?>(null) }

    // Flyer compartido desde otra app: abrir "Nuevo evento" con esa imagen
    LaunchedEffect(sharedImage) {
        if (sharedImage != null) {
            selectedId = null
            editingId = null
            editorImage = sharedImage
            showEditor = true
            onSharedImageHandled()
        }
    }
    // El estado del scroll vive aquí para no perder la posición al volver del detalle
    val gridState = rememberLazyGridState()

    AnimatedContent(
        targetState = events.find { it.id == selectedId },
        contentKey = { it?.id },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "pantalla"
    ) { selected ->
        if (selected != null) {
            BackHandler { selectedId = null }
            EventDetailScreen(
                event = selected,
                now = now,
                onBack = { selectedId = null },
                onEdit = {
                    editingId = selected.id
                    showEditor = true
                },
                onDelete = {
                    selectedId = null
                    onDeleteEvent(selected)
                },
                onAppendNotes = { text ->
                    val notes = listOfNotNull(selected.description?.takeIf { it.isNotBlank() }, text).joinToString("\n")
                    onUpdateEvent(selected.copy(description = notes))
                }
            )
        } else {
            EventListScreen(
                events = events,
                now = now,
                gridState = gridState,
                onOpen = { selectedId = it.id },
                onAdd = {
                    editingId = null
                    editorImage = null
                    showEditor = true
                }
            )
        }
    }

    if (showEditor) {
        val editing = events.find { it.id == editingId }
        EventEditor(
            initial = editing,
            initialImage = editorImage,
            onReadFlyer = onReadFlyer,
            onDismiss = { showEditor = false },
            onSave = { title, place, city, description, image, dateTimeMillis ->
                if (editing == null) {
                    onAddEvent(title, place, city, description, image, dateTimeMillis)
                } else {
                    onUpdateEvent(
                        editing.copy(
                            title = title,
                            place = place,
                            city = city,
                            description = description,
                            imagePath = image,
                            dateTimeMillis = dateTimeMillis
                        )
                    )
                }
                showEditor = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventListScreen(
    events: List<EventUi>,
    now: () -> Long,
    gridState: LazyGridState,
    onOpen: (EventUi) -> Unit,
    onAdd: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    // derivedStateOf: el reloj cambia cada segundo, pero la lista solo se reparte cuando un evento pasa
    val upcoming by remember(events) { derivedStateOf { events.filter { it.dateTimeMillis > now() } } }
    val past by remember(events) { derivedStateOf { events.filter { it.dateTimeMillis <= now() }.reversed() } }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(if (upcoming.isEmpty()) "Mis eventos" else "Próximos eventos") },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo evento") }
            )
        }
    ) { padding ->
        if (events.isEmpty()) {
            EmptyState(onAdd = onAdd, modifier = Modifier.padding(padding))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 4.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                upcoming.firstOrNull()?.let { next ->
                    item(key = next.id, span = { GridItemSpan(maxLineSpan) }) {
                        HeroCard(next, now, onClick = { onOpen(next) })
                    }
                }
                items(upcoming.drop(1), key = { it.id }) { event ->
                    PosterCard(event, now, isPast = false, onClick = { onOpen(event) })
                }
                if (past.isNotEmpty()) {
                    item(key = "pasados", span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            "Pasados",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 12.dp)
                        )
                    }
                    items(past, key = { it.id }) { event ->
                        PosterCard(event, now, isPast = true, onClick = { onOpen(event) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(event: EventUi, now: () -> Long, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(4f / 5f)) {
            FlyerImage(event.imagePath, event.title, Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize().background(FlyerScrim))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Text(
                    "EL PRÓXIMO",
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    event.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    listOf(event.formattedDateTime, event.locationText()).filter { it.isNotEmpty() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(16.dp))
                CountdownBlocks(event.dateTimeMillis, now, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun PosterCard(event: EventUi, now: () -> Long, isPast: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp)) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f)) {
            FlyerImage(event.imagePath, event.title, Modifier.fillMaxSize(), grayscale = isPast)
            Box(modifier = Modifier.fillMaxSize().background(FlyerScrim))
            if (!isPast) {
                DaysPill(event.dateTimeMillis, now, Modifier.align(Alignment.TopEnd).padding(10.dp))
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    DateFormatter.formatShortDateTimeSpanish(event.dateTimeMillis),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                if (event.city.isNotBlank()) {
                    Text(
                        event.city,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text("Aún no hay eventos", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Agrega el flyer de tu próximo concierto y empieza la cuenta regresiva.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAdd) { Text("Agregar evento") }
    }
}
