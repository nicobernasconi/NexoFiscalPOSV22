package ar.com.nexofiscal.nexofiscalposv2.models

import java.util.Date

// Filtros de entrada para el cierre de caja
data class CierreCajaFiltros(
    val desde: Date?,
    val hasta: Date?,
    val puntoVenta: Int?,
    val usuario: String?
)

// Resumen calculado del cierre de caja
data class CierreCajaResumen(
    val ventasBrutas: Double,
    val descuentos: Double,
    val notasCredito: Double,
    val ventasNetas: Double,
    val ivaTotal: Double,
    val movimientosCajaIngreso: Double,
    val movimientosCajaEgreso: Double,
    val porMedioPago: Map<String, Double>,
    val cantidadComprobantes: Int,
    val cantidadNC: Int,
    val cancelados: Int,
    val totalEsperadoEnCaja: Double
)

// Modelo de dominio de Cierre de Caja
class CierreCaja {
    var id: Int? = null
    var fecha: String? = null
    var totalVentas: Double? = null
    var totalGastos: Double? = null
    var efectivoInicial: Double? = null
    var efectivoFinal: Double? = null
    var tipoCajaId: Int? = null
    var usuario: Usuario? = null
}

// Request para subida/sincronización de cierres
data class CierreCajaUploadRequest(
    val fecha: String?,
    val totalVentas: Double?,
    val totalGastos: Double?,
    val efectivoInicial: Double?,
    val efectivoFinal: Double?,
    val tipoCajaId: Int?,
    val usuarioId: Int?
)
