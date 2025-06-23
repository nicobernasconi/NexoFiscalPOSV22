package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toAgrupacionEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Agrupacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AgrupacionManager {
    private const val TAG = "AgrupacionManager"
    private const val ENDPOINT_AGRUPACIONES = "/api/agrupaciones"

    fun obtenerAgrupaciones(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: AgrupacionListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Agrupacion?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Agrupacion>(ENDPOINT_AGRUPACIONES, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).agrupacionDao()
                val entities = allItems.toAgrupacionEntityList()
                entities.forEach { entity -> dao.insert(entity) }
                Log.d(TAG, "${entities.size} agrupaciones guardadas/actualizadas en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de agrupaciones: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface AgrupacionListCallback {
        fun onSuccess(agrupaciones: MutableList<Agrupacion?>?)
        fun onError(errorMessage: String?)
    }
}