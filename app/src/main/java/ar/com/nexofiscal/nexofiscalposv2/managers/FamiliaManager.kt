package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toFamiliaEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Familia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FamiliaManager {
    private const val TAG = "FamiliaManager"
    private const val ENDPOINT_FAMILIAS = "/api/familias"

    fun obtenerFamilias(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: FamiliaListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Familia?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Familia>(ENDPOINT_FAMILIAS, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).familiaDao()
                val entities = allItems.toFamiliaEntityList()
                entities.forEach { entity -> dao.insert(entity) }
                Log.d(TAG, "${entities.size} familias guardadas/actualizadas en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de familias: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface FamiliaListCallback {
        fun onSuccess(familias: MutableList<Familia?>?)
        fun onError(errorMessage: String?)
    }
}