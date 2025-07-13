// main/java/ar/com/nexofiscal/nexofiscalposv2/ui/ColorPickerControls.kt
package ar.com.nexofiscal.nexofiscalposv2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.BordeSuave

/**
 * Intenta convertir un String hexadecimal (ej: "#FF0000") a un objeto Color.
 * Devuelve un color por defecto si el formato es inválido.
 */
fun parseColor(hex: String?, default: Color = Color.LightGray): Color {
    if (hex.isNullOrBlank()) return default
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        default
    }
}

/**
 * Convierte un objeto Color a su representación en String hexadecimal (ej: "#FF0000").
 */
fun Color.toHexString(): String {
    return String.format("#%06X", 0xFFFFFF and this.toArgb())
}


/**
 * Un control de UI que muestra una muestra de color y su valor hexadecimal.
 * Al hacer clic, invoca la lambda `onClick`.
 */
@Composable
fun ColorSelectionField(
    label: String,
    hexColor: String?,
    isReadOnly: Boolean,
    onClick: () -> Unit
) {
    val displayColor = parseColor(hexColor)

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = BordeSuave
                )
                .clickable(enabled = !isReadOnly, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(displayColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (hexColor.isNullOrBlank()) "Seleccionar color..." else hexColor.uppercase(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/**
 * Un diálogo modal que permite al usuario seleccionar un color de una paleta
 * o introducir un código hexadecimal manualmente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerDialog(
    initialColor: String?,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val predefinedColors = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3",
        "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
        "#FFEB3B", "#FFC107", "#FF9800", "#FF5722", "#795548", "#9E9E9E"
    )

    var customColorHex by remember { mutableStateOf(initialColor ?: "#FFFFFF") }
    var previewColor by remember { mutableStateOf(parseColor(customColorHex)) }

    LaunchedEffect(customColorHex) {
        try {
            if (customColorHex.length == 7 && customColorHex.startsWith("#")) {
                previewColor = Color(android.graphics.Color.parseColor(customColorHex))
            }
        } catch (e: Exception) {
            // No hacer nada si el color es inválido mientras se tipea
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Seleccionar Color", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(predefinedColors) { colorHex ->
                        val color = parseColor(colorHex)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onColorSelected(colorHex.uppercase()) }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Divider()
                Spacer(Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(previewColor)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                    OutlinedTextField(
                        value = customColorHex,
                        onValueChange = { customColorHex = if (it.startsWith("#")) it else "#$it" },
                        label = { Text("Código Hexadecimal") },
                        placeholder = { Text("#RRGGBB") }
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onColorSelected(customColorHex.uppercase()) }) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}