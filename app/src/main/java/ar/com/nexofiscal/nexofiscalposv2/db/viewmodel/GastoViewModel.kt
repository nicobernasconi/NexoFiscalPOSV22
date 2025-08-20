package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.GastoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaResumenView
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GastoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val gastoDao = db.gastoDao()
    private val cierreDao = db.cierreCajaDao()
    private val tipoGastoDao = db.tipoGastoDao()

    fun guardarGasto(
        descripcion: String,
        monto: Double,
        fecha: String? = null,
        tipoGasto: String? = null,
        tipoGastoId: Int? = null,
        cierreCajaId: Int? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val ahora = fecha ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val tipoNombre = when {
                tipoGastoId != null -> tipoGastoDao.getById(tipoGastoId)?.nombre
                else -> tipoGasto
            }
            val entity = GastoEntity(
                id = 0,
                serverId = null,
                syncStatus = SyncStatus.CREATED,
                descripcion = descripcion,
                monto = monto,
                fecha = ahora,
                usuarioId = if (SessionManager.usuarioId > 0) SessionManager.usuarioId else null,
                empresaId = if (SessionManager.empresaId > 0) SessionManager.empresaId else null,
                tipoGastoId = tipoGastoId,
                tipoGasto = tipoNombre,
                cierreCajaId = cierreCajaId
            )
            gastoDao.insert(entity)
            // Disparar subida (si más adelante se implementa upload de Gastos)
            UploadManager.triggerImmediateUpload(getApplication())
        }
    }

    suspend fun listarCierresRecientes(): List<CierreCajaResumenView> = withContext(Dispatchers.IO) {
        cierreDao.getResumen()
    }

    suspend fun listarTiposGasto() = withContext(Dispatchers.IO) {
        tipoGastoDao.getAll()
    }

    suspend fun buscarGastos(
        desde: String,
        hasta: String,
        tipoId: Int?
    ): List<GastoEntity> = withContext(Dispatchers.IO) {
        gastoDao.getByDateRangeAndTipo(desde, hasta, tipoId)
    }

    fun rangoHoy(): Pair<String, String> {
        val sdfFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoy = sdfFecha.format(Date())
        val desde = "$hoy 00:00:00"
        val hasta = "$hoy 23:59:59"
        return desde to hasta
    }
}
