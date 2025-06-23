package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toLocalidadEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Localidad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object LocalidadManager {
    private const val TAG = "LocalidadManager"
    private const val ENDPOINT_LOCALIDADES = "/api/localidades"

    fun obtenerLocalidades(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: LocalidadListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Localidad?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Localidad>(ENDPOINT_LOCALIDADES, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).localidadDao()
                val entities = allItems.toLocalidadEntityList()
                entities.forEach { entity -> dao.insert(entity) }
                Log.d(TAG, "${entities.size} localidades guardadas/actualizadas en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de localidades: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface LocalidadListCallback {
        fun onSuccess(localidades: MutableList<Localidad?>?)
        fun onError(errorMessage: String?)
    }
}