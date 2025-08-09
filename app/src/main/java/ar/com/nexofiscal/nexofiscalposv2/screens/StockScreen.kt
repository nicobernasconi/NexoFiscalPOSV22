package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoConStockCompleto
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.StockViewModel
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    viewModel: StockViewModel = viewModel(),
    onDismiss: () -> Unit = {



    }
) {
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(searchText) {
        viewModel.updateSearchQuery(searchText)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Control de Stock",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Blanco,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Blanco
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulNexo,
                    titleContentColor = Blanco,
                    navigationIconContentColor = Blanco
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = GrisClaro
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Campo de búsqueda con estilo consistente
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = BordeSuave,
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    placeholder = {
                        Text(
                            "Buscar por código o descripción...",
                            color = TextoGrisOscuro.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = AzulNexo
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulNexo,
                        unfocusedBorderColor = GrisClaro,
                        focusedTextColor = TextoGrisOscuro,
                        unfocusedTextColor = TextoGrisOscuro
                    ),
                    shape = BordeSuave
                )
            }

            // Lista de productos con estilos consistentes
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = BordeSuave
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) {
                                "No hay productos con stock para mostrar"
                            } else {
                                "No se encontraron productos que coincidan con '$searchQuery'"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoGrisOscuro,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredProducts,
                        key = { it.productoId }
                    ) { producto ->
                        ProductoStockCard(producto = producto)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoStockCard(
    producto: ProductoConStockCompleto,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        shape = BordeSuave
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información del producto a la izquierda
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = producto.codigo ?: "Sin código",
                    style = MaterialTheme.typography.labelSmall,
                    color = AzulNexo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = producto.descripcion ?: "Sin descripción",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextoGrisOscuro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Stock mínimo y de pedido como texto simple
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Mín: ${producto.stockMinimo}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoGrisOscuro.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Ped: ${producto.stockPedido}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoGrisOscuro.copy(alpha = 0.7f)
                    )
                }
            }

            // Solo el stock actual con recuadro a la derecha
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = getStockBackgroundColor(
                    stockActual = producto.stockActual ?: 0.0,
                    stockMinimo = producto.stockMinimo.toDouble()
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = getStockTextColor(
                            stockActual = producto.stockActual ?: 0.0,
                            stockMinimo = producto.stockMinimo.toDouble()
                        ).copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${producto.stockActual?.toInt() ?: 0}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = getStockTextColor(
                            stockActual = producto.stockActual ?: 0.0,
                            stockMinimo = producto.stockMinimo.toDouble()
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun getStockBackgroundColor(stockActual: Double, stockMinimo: Double): Color {
    return when {
        stockActual <= 0 -> RojoError.copy(alpha = 0.15f)
        stockActual <= stockMinimo -> Color(0xFFFF9800).copy(alpha = 0.15f) // Naranja suave
        else -> Color(0xFF4CAF50).copy(alpha = 0.15f) // Verde suave
    }
}

@Composable
private fun getStockTextColor(stockActual: Double, stockMinimo: Double): Color {
    return when {
        stockActual <= 0 -> RojoError
        stockActual <= stockMinimo -> Color(0xFFE65100) // Naranja oscuro
        else -> Color(0xFF2E7D32) // Verde oscuro
    }
}
