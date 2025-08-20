package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaResumenView
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.repository.CierreCajaRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.ComprobanteRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.FormaPagoRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.models.CierreCajaFiltros
import ar.com.nexofiscal.nexofiscalposv2.models.CierreCajaResumen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class CierreCajaResultado(
    val cierreId: Int,
    val comprobantesAsignados: Int,
    val gastosAsignados: Int
)

class CierreCajaViewModel(application: Application) : AndroidViewModel(application) {

    private val cierreRepo: CierreCajaRepository
    private val compRepo: ComprobanteRepository
    private val formaRepo: FormaPagoRepository
    private val gastoDao = AppDatabase.getInstance(application).gastoDao()

    init {
        val db = AppDatabase.getInstance(application)
        cierreRepo = CierreCajaRepository(db.cierreCajaDao())
        compRepo = ComprobanteRepository(db.comprobanteDao())
        formaRepo = FormaPagoRepository(db.formaPagoDao())
    }

    // Exponer flujo paginado de cierres (entidad)
    fun cierresPaginated(query: String = ""): Flow<androidx.paging.PagingData<CierreCajaEntity>> =
        cierreRepo.getCierresCajaPaginated(query)

    // Nuevo: flujo paginado desde la vista de resumen (incluye usuario y comentarios)
    fun cierresResumenPaginated(): Flow<androidx.paging.PagingData<CierreCajaResumenView>> =
        cierreRepo.getCierresResumenPaginated()

    private fun ahoraStr(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun cerrarCaja(efectivoInicial: Double, efectivoFinal: Double, comentarios: String? = null): CierreCajaResultado = withContext(Dispatchers.IO) {
        val usuarioId = SessionManager.usuarioId
        require(usuarioId > 0) { "Usuario no válido para cierre de caja." }

        // Crear el cierre de caja
        val cierre = CierreCajaEntity(
            id = 0,
            serverId = null,
            syncStatus = SyncStatus.CREATED,
            fecha = ahoraStr(),
            totalVentas = null,
            totalGastos = null,
            efectivoInicial = efectivoInicial,
            efectivoFinal = efectivoFinal,
            tipoCajaId = null,
            usuarioId = usuarioId,
            comentarios = comentarios
        )
        val cierreIdLong = cierreRepo.guardar(cierre)
        val cierreId = cierreIdLong.toInt()

        // Asignar el id de cierre a los comprobantes y gastos del usuario
        val comps = compRepo.asignarCierreAComprobantesDeUsuario(usuarioId, cierreId)
        val gastos = gastoDao.asignarCierreAGastosDeUsuario(usuarioId, cierreId)

        CierreCajaResultado(cierreId = cierreId, comprobantesAsignados = comps, gastosAsignados = gastos)
    }

    // Sugerencia: último efectivoFinal del usuario como efectivo inicial
    suspend fun sugerirEfectivoInicial(): Double = withContext(Dispatchers.IO) {
        val uid = SessionManager.usuarioId
        if (uid <= 0) 0.0 else (cierreRepo.ultimoEfectivoFinalUsuario(uid) ?: 0.0)
    }

    // Calcula el resumen listo para imprimir a partir del cierre
    suspend fun generarResumenCierre(cierreId: Int): Pair<CierreCajaFiltros, CierreCajaResumen> = withContext(Dispatchers.IO) {
        val cierre = cierreRepo.porId(cierreId)
        val comprobantes = compRepo.listarPorCierre(cierreId)

        // Mapear serverId de forma de pago -> nombre (serverId coincide con comprobante_pagos.formaPagoId)
        val formas = formaRepo.getAllWithDetails()
        val nombrePorFormaId = formas.associate {
            val key = it.formaPago.serverId ?: it.formaPago.id
            val nombre = it.formaPago.nombre ?: ("Forma #" + (it.formaPago.serverId ?: it.formaPago.id))
            key to nombre
        }
        // Mapa adicional: serverId -> tipoFormaPagoId para poder filtrar CTA CTE (id=2)
        val tipoPorFormaId = formas.associate {
            val key = it.formaPago.serverId ?: it.formaPago.id
            it.formaPago.tipoFormaPagoId?.let { t -> key to t } ?: (key to -1)
        }

        var ventasBrutas = 0.0
        var descuentos = 0.0
        var notasCredito = 0.0
        var ivaTotal = 0.0
        var cantidadComprobantes = 0
        var cantidadNC = 0
        var cancelados = 0

        // Sumas por forma de pago (id del servidor)
        val pagosPorFormaId = mutableMapOf<Int, Double>()

        comprobantes.forEach { compDet ->
            val c = compDet.comprobante
            cantidadComprobantes++
            if (c.cancelado == true) cancelados++
            if (c.tipoComprobanteId == 4) cantidadNC++

            val total = c.total?.toDoubleOrNull() ?: 0.0
            val desc = c.descuento ?: 0.0
            val iva = c.importeIva ?: 0.0

            if (c.tipoComprobanteId == 4) {
                notasCredito += total
            } else if (c.cancelado != true) {
                ventasBrutas += total
                descuentos += desc
                ivaTotal += iva
            }

            // pagos (formaPagoId es serverId)
            compDet.pagos.forEach { p ->
                val tipo = tipoPorFormaId[p.formaPagoId] ?: -1
                if (tipo == 2) return@forEach // excluir CTA CTE
                pagosPorFormaId[p.formaPagoId] = (pagosPorFormaId[p.formaPagoId] ?: 0.0) + p.importe
            }
        }

        val ventasNetas = ventasBrutas - descuentos - notasCredito
        val movimientosCajaIngreso = 0.0
        val movimientosCajaEgreso = 0.0

        // Mapear a nombres
        val pagosPorNombre = pagosPorFormaId.mapKeys { (id, _) -> nombrePorFormaId[id] ?: "Forma #$id" }

        val totalEsperadoEnCaja = pagosPorNombre.values.sum()

        // Obtener total de gastos asociados al cierre
        val totalGastos = gastoDao.getTotalPorCierre(cierreId)

        val filtros = CierreCajaFiltros(
            desde = null,
            hasta = null,
            puntoVenta = SessionManager.puntoVentaNumero,
            usuario = SessionManager.nombreCompleto
        )

        val resumen = CierreCajaResumen(
            ventasBrutas = ventasBrutas,
            descuentos = descuentos,
            notasCredito = notasCredito,
            ventasNetas = ventasNetas,
            ivaTotal = ivaTotal,
            movimientosCajaIngreso = movimientosCajaIngreso,
            movimientosCajaEgreso = movimientosCajaEgreso,
            porMedioPago = pagosPorNombre,
            cantidadComprobantes = cantidadComprobantes,
            cantidadNC = cantidadNC,
            cancelados = cancelados,
            totalEsperadoEnCaja = totalEsperadoEnCaja,
            efectivoInicial = cierre?.efectivoInicial,
            efectivoFinal = cierre?.efectivoFinal,
            cierreId = cierreId,
            usuarioNombre = SessionManager.nombreCompleto,
            totalGastos = totalGastos,
            comentarios = cierre?.comentarios
        )

        filtros to resumen
    }
}
