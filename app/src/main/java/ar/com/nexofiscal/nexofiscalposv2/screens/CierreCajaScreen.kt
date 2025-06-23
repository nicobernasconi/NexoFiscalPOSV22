// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/CierreCajaScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.CierreCajaViewModel
import ar.com.nexofiscal.nexofiscalposv2.models.CierreCaja
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CierreCajaScreen(
    viewModel: CierreCajaViewModel,
    onDismiss: () -> Unit
) {
    val pagedItems = viewModel.pagedCierresCaja.collectAsLazyPagingItems()

    val itemLabel: (CierreCaja) -> String = { cierre ->
        val fechaFormateada = cierre.fecha?.let {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                formatter.format(parser.parse(it)!!)
            } catch (e: Exception) { cierre.fecha }
        } ?: "Sin fecha"

        buildString {
            appendLine("Fecha: $fechaFormateada")
            appendLine("Usuario: ${cierre.usuario?.nombreCompleto ?: "N/A"}")
            append("Total Ventas: $${String.format(Locale.getDefault(), "%.2f", cierre.totalVentas ?: 0.0)}")
        }
    }

    CrudListScreen(
        title = "Historial de Cierres de Caja",
        items = pagedItems,
        itemContent = { cierre ->
            val label = buildString {
                appendLine("Fecha: $cierre.fecha")
                appendLine("Usuario: ${cierre.usuario?.nombreCompleto ?: "N/A"}")
                append("Total Ventas: $${String.format(Locale.getDefault(), "%.2f", cierre.totalVentas ?: 0.0)}")
            }
            Text(label)
        },
        onSearchQueryChanged = { query -> viewModel.search(query) },
        onSelect = { /* Podría mostrar un detalle en el futuro */ },
        onDismiss = onDismiss,
        screenMode = CrudScreenMode.ONLY_VIEW, // Modo de solo vista
        itemKey = { it.id }
    )
}