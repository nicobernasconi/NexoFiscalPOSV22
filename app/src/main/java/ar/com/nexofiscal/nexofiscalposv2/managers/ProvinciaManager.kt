package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toProvinciaEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Provincia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ProvinciaManager {
    private const val TAG = "ProvinciaManager"
    private const val ENDPOINT_PROVINCIAS = "/api/provincias"

    fun obtenerProvincias(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: ProvinciaListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Provincia?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Provincia>(ENDPOINT_PROVINCIAS, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).provinciaDao()
                val entities = allItems.toProvinciaEntityList()
                entities.forEach { entity -> dao.insert(entity) }
                Log.d(TAG, "${entities.size} provincias guardadas/actualizadas en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de provincias: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface ProvinciaListCallback {
        fun onSuccess(provincias: MutableList<Provincia?>?)
        fun onError(errorMessage: String?)
    }
}