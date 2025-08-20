package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.managers.StockActualizacionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockAjusteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val productoDao = db.productoDao()
    private val stockDao = db.stockProductoDao()

    suspend fun buscarProductoPorCodigoOBarra(valor: String): ProductoEntity? = withContext(Dispatchers.IO) {
        val byBarcode = productoDao.findByBarcode(valor)
        if (byBarcode != null) return@withContext byBarcode
        return@withContext productoDao.findByCodigo(valor)
    }

    suspend fun cargarStockActual(producto: ProductoEntity): Double = withContext(Dispatchers.IO) {
        val sucursalId = SessionManager.sucursalId ?: 0
        // stock_productos.productoId referencia serverId del producto
        val prodServerId = producto.serverId
        if (prodServerId == null) return@withContext 0.0
        val stock = stockDao.getByProductoId(prodServerId, sucursalId)
        return@withContext stock?.stockActual ?: 0.0
    }

    suspend fun aplicarAjuste(
        context: Context,
        producto: ProductoEntity,
        cantidadAjuste: Double,
        nuevoCosto: Double?,
        actualizarPrecio1: Boolean,
        nuevoPrecio1: Double?
    ): Double = withContext(Dispatchers.IO) {
        val sucursalId = SessionManager.sucursalId ?: 0
        // Obtener/crear registro de stock_productos para este producto y sucursal
        val prodServerId = producto.serverId
            ?: throw IllegalStateException("El producto no tiene serverId asignado, no se puede ajustar stock.")
        val existente = stockDao.getByProductoId(prodServerId, sucursalId)
        val stockActual = existente?.stockActual ?: 0.0
        val nuevoStock = stockActual + cantidadAjuste

        val entidad = if (existente != null) {
            existente.copy(stockActual = nuevoStock)
        } else {
            StockProductoEntity(
                id = 0,
                serverId = null,
                syncStatus = SyncStatus.CREATED,
                codigo = null,
                stockInicial = null,
                controlaStock = true,
                puntoPedido = null,
                largo = null,
                alto = null,
                ancho = null,
                peso = null,
                unidadId = producto.unidadId,
                ubicacionId = null,
                proveedoresId = producto.proveedorId,
                productoId = prodServerId, // referencia a serverId
                empresaId = SessionManager.empresaId,
                stockActual = nuevoStock,
                sucursalId = sucursalId
            )
        }

        if (existente != null) {
            stockDao.update(entidad)
        } else {
            stockDao.insert(entidad)
        }

        // Actualizar costos/precio si corresponde
        var actualizado = false
        val modProducto = producto.copy(
            precioCosto = nuevoCosto ?: producto.precioCosto,
            precio1 = if (actualizarPrecio1 && nuevoPrecio1 != null) nuevoPrecio1 else producto.precio1
        )
        if (modProducto.precioCosto != producto.precioCosto || modProducto.precio1 != producto.precio1) {
            productoDao.update(modProducto)
            actualizado = true
        }

        // Registrar movimiento de stock (usa id local de producto)
        StockActualizacionManager.registrarActualizacionStock(context, producto.id, sucursalId, cantidadAjuste)

        return@withContext nuevoStock
    }
}

