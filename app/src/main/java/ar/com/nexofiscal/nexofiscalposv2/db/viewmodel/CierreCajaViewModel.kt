package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.repository.CierreCajaRepository
import ar.com.nexofiscal.nexofiscalposv2.db.repository.ComprobanteRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import kotlinx.coroutines.Dispatchers
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

    init {
        val db = AppDatabase.getInstance(application)
        cierreRepo = CierreCajaRepository(db.cierreCajaDao())
        compRepo = ComprobanteRepository(db.comprobanteDao())
    }

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
}

