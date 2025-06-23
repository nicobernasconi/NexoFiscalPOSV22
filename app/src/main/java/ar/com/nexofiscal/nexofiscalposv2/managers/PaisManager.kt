package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toPaisEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Pais
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PaisManager {
    private const val TAG = "PaisManager"
    private const val ENDPOINT_PAISES = "/api/paises"

    fun obtenerPaises(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: PaisListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Pais?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Pais>(ENDPOINT_PAISES, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).paisDao()
                val entities = allItems.toPaisEntityList()
                entities.forEach { entity -> dao.insert(entity) }
                Log.d(TAG, "${entities.size} países guardados/actualizados en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de países: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface PaisListCallback {
        fun onSuccess(paises: MutableList<Pais?>?)
        fun onError(errorMessage: String?)
    }
}