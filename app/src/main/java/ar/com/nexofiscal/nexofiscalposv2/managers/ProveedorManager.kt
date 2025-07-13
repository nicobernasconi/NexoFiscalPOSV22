package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toProveedorEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Proveedor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ProveedorManager {
    private const val TAG = "ProveedorManager"
    private const val ENDPOINT_PROVEEDORES = "/api/proveedores"

    fun obtenerProveedores(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: ProveedorListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Proveedor?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Proveedor>(ENDPOINT_PROVEEDORES, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).proveedorDao()
                val entities = allItems.toProveedorEntityList()
                dao.upsertAll(entities)
                Log.d(TAG, "${entities.size} proveedores guardados/actualizados en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de proveedores: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface ProveedorListCallback {
        fun onSuccess(proveedores: MutableList<Proveedor?>?)
        fun onError(errorMessage: String?)
    }
}