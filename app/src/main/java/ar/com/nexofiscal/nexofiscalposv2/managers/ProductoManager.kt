package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toProductoEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Producto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ProductoManager {
    private const val TAG = "ProductoManager"
    private const val ENDPOINT_PRODUCTO = "/api/productos"

    fun obtenerProductos(
        context: Context,
        headers: MutableMap<String?, String?>?,
        body: String?,
        callback: ProductoListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Producto?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Producto>(ENDPOINT_PRODUCTO, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).productoDao()
                val entities = allItems.toProductoEntityList()
                entities.forEach { entity -> dao.insert(entity) }
                Log.d(TAG, "${entities.size} productos guardados/actualizados en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de productos: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface ProductoListCallback {
        fun onSuccess(productos: MutableList<Producto?>?)
        fun onError(errorMessage: String?)
    }
}