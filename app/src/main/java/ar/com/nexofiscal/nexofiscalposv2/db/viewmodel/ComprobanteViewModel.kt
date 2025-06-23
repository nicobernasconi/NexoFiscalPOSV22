package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ComprobantePagoDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ComprobantePromocionDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.RenglonComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobantePagoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobantePromocionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RenglonComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toComprobanteConDetalle
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.models.Comprobante
import ar.com.nexofiscal.nexofiscalposv2.models.TipoComprobante
import ar.com.nexofiscal.nexofiscalposv2.db.repository.ComprobanteRepository
import ar.com.nexofiscal.nexofiscalposv2.models.Promocion
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import ar.com.nexofiscal.nexofiscalposv2.screens.Pago
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty

data class ComprobanteConDetalle(
    val comprobante: Comprobante,
    val cliente: Cliente?,
    val tipoComprobante: TipoComprobante?
)

class ComprobanteViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase
    private val comprobanteRepo: ComprobanteRepository
    private val comprobanteDao: ComprobanteDao
    private val comprobantePagoDao: ComprobantePagoDao
    private val comprobantePromocionDao: ComprobantePromocionDao
    private val renglonComprobanteDao: RenglonComprobanteDao // <-- AÑADIR
    private val _searchQuery = MutableStateFlow("")

    init {
        db = AppDatabase.getInstance(application)
        comprobanteDao = db.comprobanteDao()
        comprobantePagoDao = db.comprobantePagoDao()
        comprobantePromocionDao = db.comprobantePromocionDao()
        renglonComprobanteDao = db.renglonComprobanteDao() // <-- AÑADIR
        comprobanteRepo = ComprobanteRepository(comprobanteDao)
    }

    val pagedComprobantes: Flow<PagingData<ComprobanteConDetalle>> = _searchQuery
        .flatMapLatest { query ->
            comprobanteRepo.getComprobantesPaginated(query)
        }
        .map { pagingData ->
            pagingData.map { comprobanteConDetallesEntity ->
                comprobanteConDetallesEntity.toComprobanteConDetalle()
            }
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    suspend fun getNextNumeroForTipo(tipoId: Int): Int {
        return (comprobanteRepo.getHighestNumeroForTipo(tipoId) ?: 0) + 1
    }

    /**
     * Guarda la venta completa (comprobante, renglones, pagos y promociones) en una única transacción.
     */
    suspend fun saveVentaCompleta(
        comprobante: Comprobante,
        renglones: List<RenglonComprobante>,
        pagos: List<Pago>,
        promociones: List<Promocion>
    ): Long {
        var newComprobanteId: Long = -1
        val gson = Gson()

        db.runInTransaction {
            // 1. Guardar la entidad principal del comprobante y obtener su nuevo ID local
            val comprobanteEntity = comprobante.toEntity()
            newComprobanteId = comprobanteDao.insert(comprobanteEntity)

            // 2. Preparar y guardar las entidades de renglones
            if (renglones.isNotEmpty()) {
                val renglonEntities = renglones.map { renglon ->
                    RenglonComprobanteEntity(
                        comprobanteLocalId = newComprobanteId.toInt(),
                        data = gson.toJson(renglon)
                    )
                }
                renglonComprobanteDao.insertAll(renglonEntities)
            }

            // 3. Preparar y guardar las entidades de pago
            if (pagos.isNotEmpty()) {
                val pagoEntities = pagos.map { pago ->
                    ComprobantePagoEntity(
                        comprobanteLocalId = newComprobanteId,
                        formaPagoId = pago.formaPago.id,
                        importe = pago.monto,
                        syncStatus = SyncStatus.CREATED
                    )
                }
                comprobantePagoDao.insertAll(pagoEntities)
            }

            // 4. Preparar y guardar las entidades de promoción
            if (promociones.isNotEmpty()) {
                val promocionEntities = promociones.map { promocion ->
                    ComprobantePromocionEntity(
                        comprobanteLocalId = newComprobanteId,
                        promocionId = promocion.id
                    )
                }
                comprobantePromocionDao.insertAll(promocionEntities)
            }
        }
        return newComprobanteId
    }

    fun delete(c: ComprobanteEntity) {
        viewModelScope.launch {
            c.syncStatus = SyncStatus.DELETED
            comprobanteRepo.actualizar(c)
        }
    }

    suspend fun getById(id: Int): ComprobanteEntity? {
        return comprobanteRepo.porId(id)
    }
}