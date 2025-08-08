package com.wltr.linkycow.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wltr.linkycow.data.remote.dto.Link
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Archive
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LinkItem(
    link: Link,
    onLinkClick: (Int) -> Unit,
    onDelete: ((Int) -> Unit)? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: ((Int) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    // Swipe state
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Threshold für Swipe-Aktionen (in dp)
    val swipeThreshold = with(density) { 100.dp.toPx() }
    
    // Berechne Animationsparameter basierend auf dem Swipe-Fortschritt
    val deleteProgress = ((-offsetX) / swipeThreshold).coerceIn(0f, 1f)

    
    // Animierte Werte für bessere visuelle Effekte
    val cardScale by animateFloatAsState(
        targetValue = if (isDragging) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Background für Swipe-Aktionen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {

            
            Spacer(modifier = Modifier.weight(1f))
            
            // Linker Bereich: Löschen (bei Swipe nach links sichtbar)
            if (onDelete != null && offsetX < 0) {
                Box(
                    modifier = Modifier
                        .width(with(density) { (-offsetX).toDp() })
                        .fillMaxHeight()
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(
                                alpha = 0.7f + (deleteProgress * 0.3f)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Löschen",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .size((16 + (deleteProgress * 8)).dp)
                    )
                }
            }
        }
        
        // Hauptkarte
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = if (isSelectionMode) 0f else offsetX
                    scaleX = cardScale
                    scaleY = cardScale
                }
                .pointerInput(isSelectionMode) {
                    if (!isSelectionMode) {
                        detectHorizontalDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                // Überprüfung, ob Schwellenwert erreicht wurde
                                when {
                                    offsetX < -swipeThreshold && onDelete != null -> {
                                        // Direkt die Aktion ausführen, wie in LinkDetailScreen
                                        onDelete(link.id)
                                    }
                                }
                                // Zurück zur ursprünglichen Position
                                coroutineScope.launch {
                                    animate(
                                        initialValue = offsetX,
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 300)
                                    ) { value, _ ->
                                        offsetX = value
                                    }
                                }
                            }
                        ) { _, dragAmount ->
                            // Begrenze den Swipe basierend auf verfügbaren Aktionen
                            val newOffset = offsetX + dragAmount
                            offsetX = when {
                                newOffset < 0 && onDelete != null -> newOffset.coerceAtLeast(-swipeThreshold * 1.5f)
                                else -> 0f
                            }
                        }
                    }
                }
                .combinedClickable(
                    onClick = { 
                        if (!isDragging) {
                            if (isSelectionMode) {
                                onToggleSelection?.invoke(link.id)
                            } else {
                                onLinkClick(link.id)
                            }
                        }
                    },
                    onLongClick = {
                        if (!isSelectionMode && onLongClick != null) {
                            onLongClick()
                        }
                    }
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else 
                    MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Checkbox für Selection Mode
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelection?.invoke(link.id) }
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = link.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = link.url,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!link.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = link.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Tags and Collection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Collection Chip
                        link.collection?.let { collection ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Collection",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = collection.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Spacer if both are present
                        if (link.collection != null && !link.tags.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Tags Chips
                        if (!link.tags.isNullOrEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                item {
                                    Icon(
                                        imageVector = Icons.Default.Tag,
                                        contentDescription = "Tags",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                items(link.tags) { tag ->
                                    AssistChip(
                                        onClick = { /* Non-functional in this view */ },
                                        label = { Text(tag.name, style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 