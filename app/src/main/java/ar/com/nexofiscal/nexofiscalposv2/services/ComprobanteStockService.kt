package ar.com.nexofiscal.nexofiscalposv2.services

import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.RenglonComprobanteDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.ProductoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.managers.MovimientoStock
import ar.com.nexofiscal.nexofiscalposv2.managers.StockMovementManager
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Servicio que integra el control de stock con las operaciones de comprobantes
 */
class ComprobanteStockService(
    private val comprobanteDao: ComprobanteDao,
    private val renglonComprobanteDao: RenglonComprobanteDao,
    private val stockMovementManager: StockMovementManager,
    private val productoDao: ProductoDao, // NUEVO: para resolver id local
    private val gson: Gson = Gson()
) {

    companion object {
        private const val TAG = "ComprobanteStockService"

        // Tipos de comprobante que afectan stock
        private val TIPOS_QUE_REDUCEN_STOCK = setOf(
            "FACTURA", "FACTURA_A", "FACTURA_B", "FACTURA_C",
            "PEDIDO", "REMITO", "TICKET"
        )
    }

    /**
     * Crea un comprobante y actualiza el stock automáticamente
     */
    suspend fun crearComprobanteConStock(
        comprobante: ComprobanteEntity,
        renglones: List<RenglonComprobante>,
        sucursalId: Int
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creando comprobante con control de stock")

            // 1. Insertar el comprobante
            val comprobanteId = comprobanteDao.insert(comprobante).toInt()
            Log.d(TAG, "Comprobante creado con ID: $comprobanteId")

            // 2. Insertar los renglones
            for (renglon in renglones) {
                val renglonEntity = ar.com.nexofiscal.nexofiscalposv2.db.entity.RenglonComprobanteEntity(
                    comprobanteLocalId = comprobanteId,
                    data = gson.toJson(renglon)
                )
                renglonComprobanteDao.insert(renglonEntity)
            }

            // 3. Verificar si este tipo de comprobante afecta el stock
            val tipoComprobante = comprobante.letra ?: ""
            if (TIPOS_QUE_REDUCEN_STOCK.contains(tipoComprobante.uppercase())) {

                // 4. Preparar movimientos de stock
                val movimientos = renglones.mapNotNull { renglon ->
                    if (renglon.productoId != null && renglon.cantidad > 0) {
                        val productoLocal = productoDao.findByServerId(renglon.productoId)
                        val localId = productoLocal?.id
                        if (localId == null) {
                            Log.w(TAG, "[CREACION] Producto serverId=${renglon.productoId} sin id local. Se guardará serverId como fallback en stock_actualizaciones.")
                        }
                        MovimientoStock(
                            productoId = renglon.productoId, // serverId para stock_productos
                            cantidad = renglon.cantidad,
                            tipoMovimiento = when (tipoComprobante.uppercase()) {
                                "PEDIDO" -> StockMovementManager.MOVIMIENTO_PEDIDO
                                else -> StockMovementManager.MOVIMIENTO_VENTA
                            },
                            localProductoId = localId // se registra este en stock_actualizaciones
                        )
                    } else null
                }

                // 5. Procesar movimientos de stock
                val stockExitoso = stockMovementManager.procesarMovimientosComprobante(
                    productos = movimientos,
                    sucursalId = sucursalId,
                    comprobanteId = comprobanteId,
                    esAnulacion = false
                )

                if (!stockExitoso) {
                    Log.w(TAG, "Algunos movimientos de stock fallaron para comprobante $comprobanteId")
                    // Nota: Puedes decidir si revertir el comprobante o continuar
                }
            }

            Result.success(comprobanteId)

        } catch (e: Exception) {
            Log.e(TAG, "Error al crear comprobante con stock: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Anula un comprobante y restituye el stock
     */
    suspend fun anularComprobanteConStock(
        comprobanteId: Int,
        sucursalId: Int,
        motivoAnulacion: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Anulando comprobante $comprobanteId")

            // 1. Obtener el comprobante
            val comprobante = comprobanteDao.getById(comprobanteId)
                ?: return@withContext Result.failure(Exception("Comprobante no encontrado"))

            // 2. Verificar si ya está anulado
            if (!comprobante.fechaBaja.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("El comprobante ya está anulado"))
            }

            // 3. Obtener los renglones del comprobante
            val renglonesEntity = renglonComprobanteDao.getByComprobanteId(comprobanteId)
            val renglones = renglonesEntity.map {
                gson.fromJson(it.data, RenglonComprobante::class.java)
            }

            // 3.1. Obtener tipo de comprobante
            var tipoComprobante = comprobante.letra?.takeIf { it.isNotBlank() } ?: ""
            var afectaStock = TIPOS_QUE_REDUCEN_STOCK.contains(tipoComprobante.uppercase())
            if (tipoComprobante.isBlank()) {
                Log.w(TAG, "[ANULACION] No se pudo determinar el tipo de comprobante para el comprobante $comprobanteId. Se forzará restitución de stock.")
                afectaStock = true // Forzar restitución si no se puede determinar el tipo
            }
            Log.d(TAG, "[ANULACION] Comprobante $comprobanteId, tipo: $tipoComprobante, renglones: ${renglones.size}")
            // 4. Verificar si este tipo de comprobante afectó el stock
            if (afectaStock) {

                // 5. Preparar restitución de stock
                val movimientos = renglones.mapNotNull { renglon ->
                    if (renglon.productoId != null && renglon.cantidad > 0) {
                        val productoLocal = productoDao.findByServerId(renglon.productoId)
                        val localId = productoLocal?.id
                        if (localId == null) {
                            Log.w(TAG, "[ANULACION] Producto serverId=${renglon.productoId} sin id local. Se guardará serverId como fallback en stock_actualizaciones.")
                        } else {
                            Log.d(TAG, "[ANULACION] Generando movimiento de restitución serverId=${renglon.productoId} localId=$localId cantidad=${renglon.cantidad}")
                        }
                        MovimientoStock(
                            productoId = renglon.productoId, // serverId para operar stock_productos
                            cantidad = renglon.cantidad,
                            tipoMovimiento = StockMovementManager.MOVIMIENTO_ANULACION,
                            localProductoId = localId // id local real que se debe guardar en stock_actualizaciones
                        )
                    } else null
                }

                // 6. Restituir stock
                val stockExitoso = stockMovementManager.procesarMovimientosComprobante(
                    productos = movimientos,
                    sucursalId = sucursalId,
                    comprobanteId = comprobanteId,
                    esAnulacion = true
                )

                if (!stockExitoso) {
                    Log.w(TAG, "Algunos movimientos de restitución fallaron para comprobante $comprobanteId")
                } else {
                    Log.i(TAG, "[ANULACION] Movimientos de restitución procesados y enviados a StockMovementManager para comprobante $comprobanteId")
                }
            }

            // 7. Marcar comprobante como anulado
            Log.d(TAG, "[ANULACION] Estado previo LocalID=${comprobante.id} serverId=${comprobante.serverId} syncStatus=${comprobante.syncStatus}")
            val nuevoSync = if (comprobante.serverId != null) SyncStatus.UPDATED else comprobante.syncStatus
            val comprobanteAnulado = comprobante.copy(
                fechaBaja = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                motivoBaja = motivoAnulacion,
                syncStatus = nuevoSync
            )
            comprobanteDao.update(comprobanteAnulado)
            Log.d(TAG, "[ANULACION] Marcado LocalID=${comprobanteAnulado.id} serverId=${comprobanteAnulado.serverId} nuevoSyncStatus=${comprobanteAnulado.syncStatus}")

            Log.d(TAG, "Comprobante $comprobanteId anulado exitosamente")
            Result.success(true)

        } catch (e: Exception) {
            Log.e(TAG, "Error al anular comprobante: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene el resumen de stock afectado por un comprobante
     */
    suspend fun obtenerResumenStockComprobante(
        comprobanteId: Int
    ): List<ResumenStockComprobante> = withContext(Dispatchers.IO) {
        try {
            // Obtener renglones del comprobante
            val renglonesEntity = renglonComprobanteDao.getByComprobanteId(comprobanteId)
            val renglones = renglonesEntity.map {
                gson.fromJson(it.data, RenglonComprobante::class.java)
            }

            // Crear resumen
            renglones.mapNotNull { renglon ->
                if (renglon.productoId != null) {
                    ResumenStockComprobante(
                        productoId = renglon.productoId,
                        descripcionProducto = renglon.descripcion ?: "Producto ${renglon.productoId}",
                        cantidadAfectada = renglon.cantidad
                    )
                } else null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener resumen de stock: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Verifica si hay stock suficiente antes de crear un comprobante
     */
    suspend fun verificarStockSuficiente(
        renglones: List<RenglonComprobante>,
        sucursalId: Int
    ): VerificacionStock = withContext(Dispatchers.IO) {
        try {
            val productosInsuficientes = mutableListOf<ProductoStockInsuficiente>()

            for (renglon in renglones) {
                if (renglon.productoId != null && renglon.cantidad > 0) {
                    val historial = stockMovementManager.obtenerHistorialMovimientos(
                        productoId = renglon.productoId,
                        sucursalId = sucursalId
                    )

                    // Calcular stock actual sumando todos los movimientos
                    val stockActual = historial.sumOf { it.cantidad }

                    if (stockActual < renglon.cantidad) {
                        productosInsuficientes.add(
                            ProductoStockInsuficiente(
                                productoId = renglon.productoId,
                                descripcion = renglon.descripcion ?: "Producto ${renglon.productoId}",
                                stockActual = stockActual,
                                cantidadSolicitada = renglon.cantidad,
                                faltante = renglon.cantidad - stockActual
                            )
                        )
                    }
                }
            }

            VerificacionStock(
                suficiente = productosInsuficientes.isEmpty(),
                productosInsuficientes = productosInsuficientes
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar stock: ${e.message}", e)
            VerificacionStock(
                suficiente = false,
                productosInsuficientes = emptyList()
            )
        }
    }
}

/**
 * Clases de datos para el servicio
 */
data class ResumenStockComprobante(
    val productoId: Int,
    val descripcionProducto: String,
    val cantidadAfectada: Double
)

data class VerificacionStock(
    val suficiente: Boolean,
    val productosInsuficientes: List<ProductoStockInsuficiente>
)

data class ProductoStockInsuficiente(
    val productoId: Int,
    val descripcion: String,
    val stockActual: Double,
    val cantidadSolicitada: Double,
    val faltante: Double
)
