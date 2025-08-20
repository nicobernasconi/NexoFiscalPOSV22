package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.StockMovimientosViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockActualizacionEntity
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockMovimientosScreen(
    viewModel: StockMovimientosViewModel = viewModel(),
    onDismiss: () -> Unit = {}
) {
    val movimientos by viewModel.movimientos.collectAsState()
    val estadisticas by viewModel.estadisticas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(Unit) {
        viewModel.cargarMovimientos()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Movimientos de Stock",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Blanco,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.clip(BordeSuave)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Blanco
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarMovimientos() }, modifier = Modifier.clip(BordeSuave)) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar",
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
            // Tarjeta de estadísticas
            EstadisticasCard(estadisticas)

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AzulNexo)
                }
            } else if (movimientos.isEmpty()) {
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
                            text = "No hay movimientos de stock registrados",
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
                        items = movimientos,
                        key = { it.id }
                    ) { movimiento ->
                        MovimientoCard(movimiento = movimiento)
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadisticasCard(estadisticas: EstadisticasStock) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = BordeSuave
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Estadísticas de Movimientos",
                style = MaterialTheme.typography.titleMedium,
                color = AzulNexo,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaItem(
                    label = "Total",
                    valor = estadisticas.totalMovimientos.toString(),
                    color = TextoGrisOscuro
                )
                EstadisticaItem(
                    label = "Pendientes",
                    valor = estadisticas.pendientesEnvio.toString(),
                    color = Color(0xFFFF9800)
                )
                EstadisticaItem(
                    label = "Enviados",
                    valor = estadisticas.enviados.toString(),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun EstadisticaItem(
    label: String,
    valor: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextoGrisOscuro.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun MovimientoCard(
    movimiento: StockActualizacionEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        shape = BordeSuave
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información del movimiento
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Producto ID: ${movimiento.productoId}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AzulNexo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatearFecha(movimiento.fechaCreacion),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoGrisOscuro.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Sucursal: ${movimiento.sucursalId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoGrisOscuro.copy(alpha = 0.7f)
                )
            }

            // Cantidad del movimiento
            Surface(
                shape = BordeSuave,
                color = if (movimiento.cantidad >= 0) {
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                } else {
                    Color(0xFFFF5722).copy(alpha = 0.1f)
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (movimiento.cantidad >= 0) "+" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (movimiento.cantidad >= 0) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFFF5722)
                        }
                    )
                    Text(
                        text = "${movimiento.cantidad.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (movimiento.cantidad >= 0) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFFF5722)
                        }
                    )
                }
            }

            // Estado de envío
            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = BordeSuave,
                color = if (movimiento.enviado) {
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                } else {
                    Color(0xFFFF9800).copy(alpha = 0.1f)
                }
            ) {
                Text(
                    text = if (movimiento.enviado) "✓" else "⏳",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (movimiento.enviado) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFFFF9800)
                    },
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

private fun formatearFecha(fecha: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(fecha)
}

// Clase de datos para estadísticas
data class EstadisticasStock(
    val totalMovimientos: Int = 0,
    val pendientesEnvio: Int = 0,
    val enviados: Int = 0
)
