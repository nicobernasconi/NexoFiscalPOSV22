package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity
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
    val comprobantesAsignados: Int
)

class CierreCajaViewModel(application: Application) : AndroidViewModel(application) {

    private val cierreRepo: CierreCajaRepository
    private val compRepo: ComprobanteRepository
    private val formaRepo: FormaPagoRepository

    init {
        val db = AppDatabase.getInstance(application)
        cierreRepo = CierreCajaRepository(db.cierreCajaDao())
        compRepo = ComprobanteRepository(db.comprobanteDao())
        formaRepo = FormaPagoRepository(db.formaPagoDao())
    }

    // Exponer flujo paginado de cierres de caja (ordenado por fecha desc segun DAO)
    fun cierresPaginated(query: String = ""): Flow<androidx.paging.PagingData<CierreCajaEntity>> =
        cierreRepo.getCierresCajaPaginated(query)

    private fun ahoraStr(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun cerrarCaja(efectivoInicial: Double, efectivoFinal: Double): CierreCajaResultado = withContext(Dispatchers.IO) {
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
            usuarioId = usuarioId
        )
        val cierreIdLong = cierreRepo.guardar(cierre)
        val cierreId = cierreIdLong.toInt()

        // Asignar el id de cierre a los comprobantes del usuario
        val asignados = compRepo.asignarCierreAComprobantesDeUsuario(usuarioId, cierreId)

        CierreCajaResultado(cierreId = cierreId, comprobantesAsignados = asignados)
    }

    // Calcula el resumen listo para imprimir a partir del cierre
    suspend fun generarResumenCierre(cierreId: Int): Pair<CierreCajaFiltros, CierreCajaResumen> = withContext(Dispatchers.IO) {
        val cierre = cierreRepo.porId(cierreId)
        val comprobantes = compRepo.listarPorCierre(cierreId)

        // Mapear id de forma de pago -> nombre
        val formas = formaRepo.getAllWithDetails()
        val nombrePorFormaId = formas.associate { it.formaPago.id to (it.formaPago.nombre ?: ("Forma #" + it.formaPago.id)) }

        var ventasBrutas = 0.0
        var descuentos = 0.0
        var notasCredito = 0.0
        var ivaTotal = 0.0
        var cantidadComprobantes = 0
        var cantidadNC = 0
        var cancelados = 0

        // Sumas por forma de pago (id)
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

            // pagos
            compDet.pagos.forEach { p ->
                pagosPorFormaId[p.formaPagoId] = (pagosPorFormaId[p.formaPagoId] ?: 0.0) + p.importe
            }
        }

        val ventasNetas = ventasBrutas - descuentos - notasCredito
        val movimientosCajaIngreso = 0.0
        val movimientosCajaEgreso = 0.0

        // Mapear a nombres
        val pagosPorNombre = pagosPorFormaId.mapKeys { (id, _) -> nombrePorFormaId[id] ?: "Forma #$id" }

        val totalEsperadoEnCaja = pagosPorNombre.values.sum()

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
            usuarioNombre = SessionManager.nombreCompleto
        )

        filtros to resumen
    }
}
