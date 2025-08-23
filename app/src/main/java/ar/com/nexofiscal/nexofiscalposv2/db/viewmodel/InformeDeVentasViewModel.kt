// main/java/ar/com/nexofiscal/nexofiscalposv2/db/viewmodel/InformeDeVentasViewModel.kt

package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toComprobanteConDetalle
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.models.TipoComprobante
import ar.com.nexofiscal.nexofiscalposv2.models.Usuario
import ar.com.nexofiscal.nexofiscalposv2.models.Vendedor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Estado para los filtros del informe
data class InformeFiltros(
    val fechaDesde: Date? = null,
    val fechaHasta: Date? = null,
    val tipoComprobante: TipoComprobante? = null,
    val cliente: Cliente? = null,
    val usuario: Usuario? = null,
    val vendedor: Vendedor? = null
)

// Estado para los resultados del informe
data class InformeResultados(
    val comprobantes: List<ComprobanteConDetalle> = emptyList(),
    val totalVentas: Double = 0.0
)

class InformeDeVentasViewModel(application: Application) : AndroidViewModel(application) {
    private val comprobanteDao: ComprobanteDao = AppDatabase.getInstance(application).comprobanteDao()

    private val _filtros = MutableStateFlow(InformeFiltros())
    val filtros = _filtros.asStateFlow()

    private val _resultados = MutableStateFlow(InformeResultados())
    val resultados = _resultados.asStateFlow()

    fun actualizarFiltro(nuevoFiltro: InformeFiltros) {
        _filtros.value = nuevoFiltro
    }

    // --- NUEVA FUNCIÓN ---
    fun limpiarFiltros() {
        _filtros.value = InformeFiltros()
        _resultados.value = InformeResultados()
    }

    fun ejecutarInforme() {
        viewModelScope.launch {
            val filtrosActuales = _filtros.value
            val fechaDesdeStr = filtrosActuales.fechaDesde?.let {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
            }
            val fechaHastaStr = filtrosActuales.fechaHasta?.let {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
            }

            // --- LÓGICA DE FILTROS POR DEFECTO ---
            val tiposComprobanteIds = if (filtrosActuales.tipoComprobante == null) {
                listOf(1, 3) // Por defecto: Venta (1) y Pedido (3)
            } else {
                listOf(filtrosActuales.tipoComprobante.id)
            }

            val comprobantesEntities = comprobanteDao.getComprobantesParaInforme(
                fechaDesde = fechaDesdeStr,
                fechaHasta = fechaHastaStr,
                tipoComprobanteIds = tiposComprobanteIds,
                clienteId = filtrosActuales.cliente?.id,
                vendedorId = filtrosActuales.vendedor?.id,
                usuarioId = filtrosActuales.usuario?.id
            )

            val comprobantesResult = comprobantesEntities.map { it.toComprobanteConDetalle() }
            val total = comprobantesResult.sumOf { it.comprobante.total?.toDoubleOrNull() ?: 0.0 }

            _resultados.update {
                it.copy(
                    comprobantes = comprobantesResult,
                    totalVentas = total
                )
            }
        }
    }
}