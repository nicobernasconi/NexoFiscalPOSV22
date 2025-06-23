// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/PluDirectosScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ProductoViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.Producto
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.Blanco
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.CC
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.GrisClaro
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.TextoGrisOscuro

/**
 * Parsea un string de color hexadecimal (ej: "#RRGGBB") a un objeto Color de Compose.
 * Si el string es nulo, vacío o inválido, devuelve un color por defecto.
 */
private fun parseColor(colorString: String?, defaultColor: Color): Color {
    if (colorString.isNullOrBlank() || !colorString.startsWith("#")) {
        return defaultColor
    }
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: IllegalArgumentException) {
        Log.w("ColorParser", "No se pudo parsear el color: $colorString", e)
        defaultColor
    }
}

/**
 * Determina si el texto sobre un color de fondo debe ser blanco o negro para asegurar legibilidad.
 */
private fun getTextColorForBackground(backgroundColor: Color): Color {
    val red = backgroundColor.red * 255
    val green = backgroundColor.green * 255
    val blue = backgroundColor.blue * 255
    // Fórmula de luminancia
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
    return if (luminance > 0.5) Color.Black else Color.White
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluDirectosScreen(
    productoViewModel: ProductoViewModel,
    onProductSelected: (producto: Producto) -> Unit,
    onDismiss: () -> Unit
) {
    val productosFavoritos by productoViewModel.favoritos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PLU Directos", color = Blanco) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CC)
            )
        },
        containerColor = Blanco
    ) { paddingValues ->
        if (productosFavoritos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay productos marcados como favoritos.",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = TextoGrisOscuro
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productosFavoritos, key = { it.id }) { producto ->
                    ProductoFavoritoItem(
                        producto = producto,
                        onClick = { onProductSelected(producto) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFavoritoItem(
    producto: Producto,
    onClick: () -> Unit
) {
    // ---- MODIFICACIÓN PRINCIPAL ----
    // 1. Obtenemos el color de la agrupación o usamos GrisClaro por defecto.
    val cardColor = parseColor(producto.agrupacion?.color, GrisClaro)
    // 2. Determinamos si el texto debe ser blanco o negro para que sea legible.
    val textColor = getTextColorForBackground(cardColor)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // 3. Aplicamos el color dinámico a la tarjeta.
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = producto.descripcion ?: "Producto sin descripción",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                // 4. Aplicamos el color de texto dinámico.
                color = textColor,
                modifier = Modifier.heightIn(min = 40.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = String.format("$%.2f", producto.precio1),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                // 5. Aplicamos el color de texto dinámico también al precio.
                color = textColor.copy(alpha = 0.85f) // Un poco más sutil
            )
        }
    }
}