package ar.com.nexofiscal.nexofiscalposv2.managers

import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.dao.StockActualizacionDao
import ar.com.nexofiscal.nexofiscalposv2.db.dao.StockProductoDao
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockActualizacionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Manager para controlar los movimientos de stock y registrarlos en stock_actualizaciones
 */
class StockMovementManager(
    private val stockActualizacionDao: StockActualizacionDao,
    private val stockProductoDao: StockProductoDao
) {

    companion object {
        private const val TAG = "StockMovementManager"

        // Tipos de movimiento
        const val MOVIMIENTO_VENTA = "VENTA"
        const val MOVIMIENTO_ANULACION = "ANULACION"
        const val MOVIMIENTO_PEDIDO = "PEDIDO"
        const val MOVIMIENTO_AJUSTE = "AJUSTE"
    }

    /**
     * Reduce el stock de productos por venta o pedido
     */
    suspend fun reducirStock(
        productoId: Int,
        cantidad: Double,
        sucursalId: Int,
        comprobanteId: Int,
        tipoMovimiento: String = MOVIMIENTO_VENTA
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Reduciendo stock - Producto: $productoId, Cantidad: $cantidad")

            // Buscar el stock del producto
            val stockProducto = stockProductoDao.getByProductoId(productoId, sucursalId)

            if (stockProducto == null) {
                Log.w(TAG, "No se encontró stock para producto $productoId en sucursal $sucursalId. Registrando sólo movimiento.")
                // Registrar movimiento igualmente (negativo por reducción)
                registrarMovimiento(
                    productoId = productoId,
                    sucursalId = sucursalId,
                    cantidad = -cantidad,
                    tipoMovimiento = tipoMovimiento,
                    comprobanteId = comprobanteId
                )
                return@withContext true
            }

            val stockActual = stockProducto.stockActual ?: 0.0
            val nuevoStock = stockActual - cantidad

            // Nota: permitir stock negativo según política; si no, retornar false aquí
            val stockActualizado = stockProducto.copy(stockActual = nuevoStock)
            stockProductoDao.update(stockActualizado)

            // Registrar el movimiento (cantidad negativa para reducción)
            registrarMovimiento(
                productoId = productoId,
                sucursalId = sucursalId,
                cantidad = -cantidad, // Negativo para reducción
                tipoMovimiento = tipoMovimiento,
                comprobanteId = comprobanteId
            )

            Log.d(TAG, "Stock reducido exitosamente. Nuevo stock: $nuevoStock")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Error al reducir stock: ${e.message}", e)
            false
        }
    }

    /**
     * Restituye el stock cuando se anula un comprobante
     */
    suspend fun restituirStock(
        productoId: Int,
        cantidad: Double,
        sucursalId: Int,
        comprobanteId: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Restituyendo stock - Producto: $productoId, Cantidad: $cantidad")

            // Buscar el stock del producto
            val stockProducto = stockProductoDao.getByProductoId(productoId, sucursalId)

            if (stockProducto == null) {
                Log.w(TAG, "No se encontró stock para producto $productoId en sucursal $sucursalId. Registrando sólo movimiento.")
                // Registrar movimiento igualmente (positivo por restitución)
                registrarMovimiento(
                    productoId = productoId,
                    sucursalId = sucursalId,
                    cantidad = cantidad,
                    tipoMovimiento = MOVIMIENTO_ANULACION,
                    comprobanteId = comprobanteId
                )
                return@withContext true
            }

            val stockActual = stockProducto.stockActual ?: 0.0
            val nuevoStock = stockActual + cantidad

            // Actualizar el stock
            val stockActualizado = stockProducto.copy(stockActual = nuevoStock)
            stockProductoDao.update(stockActualizado)

            // Registrar el movimiento (cantidad positiva para restitución)
            registrarMovimiento(
                productoId = productoId,
                sucursalId = sucursalId,
                cantidad = cantidad, // Positivo para restitución
                tipoMovimiento = MOVIMIENTO_ANULACION,
                comprobanteId = comprobanteId
            )

            Log.d(TAG, "Stock restituido exitosamente. Nuevo stock: $nuevoStock")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Error al restituir stock: ${e.message}", e)
            false
        }
    }

    /**
     * Procesa múltiples productos de un comprobante
     */
    suspend fun procesarMovimientosComprobante(
        productos: List<MovimientoStock>,
        sucursalId: Int,
        comprobanteId: Int,
        esAnulacion: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            var todosExitosos = true

            for (movimiento in productos) {
                val exitoso = if (esAnulacion) {
                    restituirStock(
                        productoId = movimiento.productoId,
                        cantidad = movimiento.cantidad,
                        sucursalId = sucursalId,
                        comprobanteId = comprobanteId
                    )
                } else {
                    reducirStock(
                        productoId = movimiento.productoId,
                        cantidad = movimiento.cantidad,
                        sucursalId = sucursalId,
                        comprobanteId = comprobanteId,
                        tipoMovimiento = movimiento.tipoMovimiento
                    )
                }

                if (!exitoso) {
                    todosExitosos = false
                    Log.e(TAG, "Error en movimiento para producto ${movimiento.productoId}")
                }
            }

            todosExitosos

        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar movimientos del comprobante: ${e.message}", e)
            false
        }
    }

    /**
     * Registra un movimiento en la tabla stock_actualizaciones
     */
    private suspend fun registrarMovimiento(
        productoId: Int,
        sucursalId: Int,
        cantidad: Double,
        tipoMovimiento: String,
        comprobanteId: Int? = null
    ) {
        try {
            val movimiento = StockActualizacionEntity(
                productoId = productoId,
                sucursalId = sucursalId,
                cantidad = cantidad,
                fechaCreacion = Date(),
                enviado = false,
                intentos = 0,
                ultimoError = null
            )

            stockActualizacionDao.insert(movimiento)
            Log.d(TAG, "Movimiento registrado: $tipoMovimiento - Producto: $productoId, Cantidad: $cantidad")

        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar movimiento: ${e.message}", e)
        }
    }

    /**
     * Obtiene el historial de movimientos de un producto
     */
    suspend fun obtenerHistorialMovimientos(
        productoId: Int,
        sucursalId: Int? = null
    ): List<StockActualizacionEntity> = withContext(Dispatchers.IO) {
        try {
            if (sucursalId != null) {
                stockActualizacionDao.getByProductoAndSucursal(productoId, sucursalId)
            } else {
                stockActualizacionDao.getByProducto(productoId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener historial: ${e.message}", e)
            emptyList()
        }
    }
}

/**
 * Clase de datos para representar un movimiento de stock
 */
data class MovimientoStock(
    val productoId: Int,
    val cantidad: Double,
    val tipoMovimiento: String = StockMovementManager.MOVIMIENTO_VENTA
)
