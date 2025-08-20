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
    val totalEsperadoEnCaja: Double,
    // Campos adicionales para impresión
    val efectivoInicial: Double? = null,
    val efectivoFinal: Double? = null,
    val cierreId: Int? = null,
    val usuarioNombre: String? = null,
    // Nuevo: total de gastos asociados al cierre
    val totalGastos: Double = 0.0,
    // Nuevo: comentarios del cierre (opcional)
    val comentarios: String? = null
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
