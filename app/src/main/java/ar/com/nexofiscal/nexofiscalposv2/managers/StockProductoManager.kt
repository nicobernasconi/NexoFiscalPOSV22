package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toStockProductoEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.StockProducto
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Headers

object StockProductoManager {
    private const val TAG = "StockProductoManager"
    // Endpoint puede depender de producto y/o sucursal.
    // Ej: /api/productos/{productoId}/stock o /api/sucursales/{sucursalId}/stock
    // Usamos uno general por ahora.
    private const val ENDPOINT_STOCK_PRODUCTOS = "/api/stocks"

    fun obtenerStockProductos(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: StockProductoListCallback
        // Podrías añadir productoId o sucursalId si el endpoint lo requiere
    ) {
        val stockProductoListType = object :
            com.google.gson.reflect.TypeToken<MutableList<StockProducto?>?>() {}.type

        ApiClient.request(
            HttpMethod.GET,
            ENDPOINT_STOCK_PRODUCTOS, // Ajusta si es necesario
            headers,
            null,
            stockProductoListType,
            object : ApiCallback<MutableList<StockProducto?>?> {
                override fun onSuccess(
                    statusCode: Int,
                    responseHeaders: Headers?,
                    stockProductos: MutableList<StockProducto?>?
                ) {
                    callback.onSuccess(stockProductos)

                    stockProductos?.let { listaStock ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val stockDao = AppDatabase.getInstance(context.applicationContext).stockProductoDao()
                                val stockEntities = listaStock.toStockProductoEntityList()
                                stockEntities.forEach { entity ->
                                    stockDao.insert(entity)
                                }
                                Log.d(TAG, "${stockEntities.size} registros de stock guardados/actualizados.")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al guardar stock en la BD: ${e.message}", e)
                            }
                        }
                    }
                }

                override fun onError(statusCode: Int, errorMessage: String?) {
                    Log.e(
                        TAG,
                        "Error al obtener stock de productos. Código: $statusCode, Mensaje: $errorMessage"
                    )
                    callback.onError(errorMessage)
                }
            }
        )
    }

    interface StockProductoListCallback {
        fun onSuccess(stockProductos: MutableList<StockProducto?>?)
        fun onError(errorMessage: String?)
    }
}