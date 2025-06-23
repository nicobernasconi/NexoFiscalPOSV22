package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toVendedorEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Vendedor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object VendedorManager {
    private const val TAG = "VendedorManager"
    private const val ENDPOINT_VENDEDORES = "/api/vendedores"

    fun obtenerVendedores(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: VendedorListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Vendedor?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Vendedor>(ENDPOINT_VENDEDORES, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).vendedorDao()
                val entities = allItems.toVendedorEntityList()
                entities.forEach { entity -> dao.insert(entity) }
                Log.d(TAG, "${entities.size} vendedores guardados/actualizados en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de vendedores: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface VendedorListCallback {
        fun onSuccess(vendedores: MutableList<Vendedor?>?)
        fun onError(errorMessage: String?)
    }
}