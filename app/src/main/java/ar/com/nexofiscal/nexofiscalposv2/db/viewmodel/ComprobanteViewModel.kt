package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import android.util.Log
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
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toNewLocalEntity
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.models.Comprobante
import ar.com.nexofiscal.nexofiscalposv2.models.TipoComprobante
import ar.com.nexofiscal.nexofiscalposv2.db.repository.ComprobanteRepository
import ar.com.nexofiscal.nexofiscalposv2.managers.UploadManager
import ar.com.nexofiscal.nexofiscalposv2.models.Promocion
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import ar.com.nexofiscal.nexofiscalposv2.models.Vendedor
import ar.com.nexofiscal.nexofiscalposv2.screens.Pago
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.isNotEmpty
import ar.com.nexofiscal.nexofiscalposv2.managers.StockMovementManager
import ar.com.nexofiscal.nexofiscalposv2.managers.MovimientoStock
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.screens.services.AfipService
import ar.com.nexofiscal.nexofiscalposv2.utils.PrintingManager
import ar.com.nexofiscal.nexofiscalposv2.utils.PrintingException
import ar.com.nexofiscal.nexofiscalposv2.managers.StockActualizacionManager
import ar.com.nexofiscal.nexofiscalposv2.services.ComprobanteStockService

data class ComprobanteConDetalle(
    val comprobante: Comprobante,
    val cliente: Cliente?,
    val tipoComprobante: TipoComprobante?,
    val vendedor: Vendedor? // Asumiendo que también tienes un vendedor relacionado
)

class ComprobanteViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase
    private val comprobanteRepo: ComprobanteRepository
    private val comprobanteDao: ComprobanteDao
    private val comprobantePagoDao: ComprobantePagoDao
    private val comprobantePromocionDao: ComprobantePromocionDao
    private val renglonComprobanteDao: RenglonComprobanteDao // <-- AÑADIR
    private val _searchQuery = MutableStateFlow("")
    private val stockMovementManager: StockMovementManager
    private lateinit var comprobanteStockService: ComprobanteStockService

    init {
        db = AppDatabase.getInstance(application)
        comprobanteDao = db.comprobanteDao()
        comprobantePagoDao = db.comprobantePagoDao()
        comprobantePromocionDao = db.comprobantePromocionDao()
        renglonComprobanteDao = db.renglonComprobanteDao() // <-- AÑADIR
        comprobanteRepo = ComprobanteRepository(comprobanteDao)
        stockMovementManager = StockMovementManager(db.stockActualizacionDao(), db.stockProductoDao())
        // Actualizado: se pasa productoDao para usar id local en movimientos (creación/anulación)
        comprobanteStockService = ComprobanteStockService(
            comprobanteDao = comprobanteDao,
            renglonComprobanteDao = renglonComprobanteDao,
            stockMovementManager = stockMovementManager,
            productoDao = db.productoDao()
        )
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
            comprobanteEntity.syncStatus = SyncStatus.CREATED
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

            // 4. Preparar y guardar las entidades de promoci��n
            if (promociones.isNotEmpty()) {
                val promocionEntities = promociones.map { promocion ->
                    ComprobantePromocionEntity(
                        comprobanteLocalId = newComprobanteId.toInt(),
                        promocionId = promocion.id
                    )
                }
                comprobantePromocionDao.insertAll(promocionEntities)

            }
        }
        // Registrar movimientos de stock según tipo de comprobante
        if (newComprobanteId > 0) {
            val tipoId = comprobante.tipoComprobanteId
            if (tipoId == 1 || tipoId == 3) { // 1: Venta, 3: Pedido
                val movimientos = renglones.mapNotNull { renglon ->
                    val pidServer = renglon.productoId // id remoto (server) para operaciones sobre stock_productos
                    val localPid = renglon.producto?.localId // id local Room
                    val cant = renglon.cantidad
                    if (pidServer != null && cant > 0) {
                        MovimientoStock(
                            productoId = pidServer,
                            cantidad = cant,
                            tipoMovimiento = if (tipoId == 3) StockMovementManager.MOVIMIENTO_PEDIDO else StockMovementManager.MOVIMIENTO_VENTA,
                            localProductoId = localPid
                        )
                    } else null
                }
                val sucursalId = SessionManager.sucursalId ?: 0
                // Ejecutar fuera de la transacción principal
                stockMovementManager.procesarMovimientosComprobante(
                    productos = movimientos,
                    sucursalId = sucursalId,
                    comprobanteId = newComprobanteId.toInt(),
                    esAnulacion = false
                )
                // Nuevo: disparar envío de actualizaciones de stock si hay token
                SessionManager.token?.let { token ->
                    val headers = mutableMapOf<String?, String?>("Authorization" to "Bearer $token")
                    StockActualizacionManager.enviarActualizacionesPendientes(getApplication(), headers)
                }
            }
        }
        UploadManager.triggerImmediateUpload(getApplication())
        return newComprobanteId
    }

    fun anularComprobante(comprobante: Comprobante) {
        viewModelScope.launch {
            try {
                val esNotaCredito = (comprobante.tipoFactura in listOf(3,8,13,53)) || comprobante.tipoComprobanteId == 4
                if (esNotaCredito) {
                    NotificationManager.show("No se puede anular una Nota de Crédito.", NotificationType.ERROR)
                    return@launch
                }
                val esFacturaElectronica = comprobante.tipoFactura in listOf(1,6,11,51)
                var notaCreditoCreada = false
                if (esFacturaElectronica) {
                    if (comprobante.cae.isNullOrBlank()) {
                        NotificationManager.show("No se puede generar NC: comprobante sin CAE.", NotificationType.ERROR)
                    } else if (comprobante.numeroFactura == null) {
                        NotificationManager.show("No se puede generar NC: comprobante sin número.", NotificationType.ERROR)
                    } else {
                        try {
                            NotificationManager.show("Generando Nota de Crédito...", NotificationType.INFO)
                            val nc = AfipService.procesarNotaCredito(comprobante)
                            Log.d("ComprobanteViewModel", "NC generada en memoria (antes de insertar): numeroFactura=${nc.numeroFactura} tipoFactura=${nc.tipoFactura} tipoComprobanteId=${nc.tipoComprobanteId} total=${nc.total}")
                            val (ncLocalId, clonados) = withContext(Dispatchers.IO) {
                                val ncEntity = nc.toNewLocalEntity()
                                Log.d("ComprobanteViewModel", "Insertando NC entity -> tipoFactura=${ncEntity.tipoFactura} tipoComprobanteId=${ncEntity.tipoComprobanteId} numeroFactura=${ncEntity.numeroFactura}")
                                val newId = db.comprobanteDao().insert(ncEntity).toInt()
                                val originalesEntities = renglonComprobanteDao.getByComprobanteId(comprobante.localId)
                                if (originalesEntities.isNotEmpty()) {
                                    renglonComprobanteDao.insertAll(originalesEntities.map { ent ->
                                        RenglonComprobanteEntity(
                                            comprobanteLocalId = newId,
                                            data = ent.data
                                        )
                                    })
                                }
                                newId to originalesEntities.size
                            }
                            Log.d("ComprobanteViewModel", "NC insertada OK localId=$ncLocalId renglonesClonados=$clonados")

                            // Impresión (fuera de IO)
                            try {
                                val renglonesDominio = withContext(Dispatchers.IO) {
                                    renglonComprobanteDao.getByComprobanteId(ncLocalId).map { ent -> Gson().fromJson(ent.data, RenglonComprobante::class.java) }
                                }
                                PrintingManager.print(
                                    context = getApplication(),
                                    comprobante = nc.copy(localId = ncLocalId),
                                    renglones = renglonesDominio
                                )
                                PrintingManager.printAsPdf(
                                    context = getApplication(),
                                    comprobante = nc.copy(localId = ncLocalId),
                                    renglones = renglonesDominio
                                )
                            } catch (pe: PrintingException) {
                                Log.e("ComprobanteViewModel", "Error impresión NC: ${pe.message}")
                                NotificationManager.show(pe.message ?: "Error imprimiendo NC", NotificationType.ERROR)
                            } catch (e: Exception) {
                                Log.e("ComprobanteViewModel", "Excepción impresión NC: ${e.message}")
                                NotificationManager.show("Error impresión NC", NotificationType.ERROR)
                            }
                            UploadManager.triggerImmediateUpload(getApplication())
                            notaCreditoCreada = true
                            NotificationManager.show("NC creada, CAE obtenido e impresa.", NotificationType.SUCCESS)
                        } catch (e: Exception) {
                            Log.e("ComprobanteViewModel", "Fallo creando NC: ${e.message}", e)
                            NotificationManager.show("Error al crear NC: ${e.message}", NotificationType.ERROR)
                            return@launch
                        }
                    }
                }

                if (!esFacturaElectronica || notaCreditoCreada) {
                    val sucursalId = SessionManager.sucursalId ?: 0
                    val resultado = comprobanteStockService.anularComprobanteConStock(
                        comprobanteId = comprobante.localId,
                        sucursalId = sucursalId,
                        motivoAnulacion = "Anulación desde app"
                    )
                    if (resultado.isSuccess) {
                        // Disparar envío de actualizaciones de stock
                        SessionManager.token?.let { token ->
                            val headers = mutableMapOf<String?, String?>("Authorization" to "Bearer $token")
                            StockActualizacionManager.enviarActualizacionesPendientes(getApplication(), headers)
                        }
                        UploadManager.triggerImmediateUpload(getApplication())
                        NotificationManager.show("Comprobante anulado y stock restituido.", NotificationType.SUCCESS)
                    } else {
                        NotificationManager.show("Error al anular: ${resultado.exceptionOrNull()?.message}", NotificationType.ERROR)
                    }
                }
            } catch (e: Exception) {
                Log.e("ComprobanteViewModel", "Error general anulación: ${e.message}", e)
                NotificationManager.show("Error al anular: ${e.message}", NotificationType.ERROR)
            }
        }
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