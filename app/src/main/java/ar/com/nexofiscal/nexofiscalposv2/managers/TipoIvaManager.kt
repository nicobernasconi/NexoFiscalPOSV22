package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toTipoIvaEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.TipoIVA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TipoIvaManager {
    private const val TAG = "TipoIvaManager"
    private const val ENDPOINT_TIPOS_IVA = "/api/tipos_iva"

    fun obtenerTiposIva(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: TipoIvaListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<TipoIVA?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<TipoIVA>(ENDPOINT_TIPOS_IVA, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).tipoIvaDao()
                val entities = allItems.toTipoIvaEntityList()
                entities.forEach { entity -> dao.insert(entity) }
                Log.d(TAG, "${entities.size} tipos de IVA guardados/actualizados en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de tipos de IVA: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface TipoIvaListCallback {
        fun onSuccess(tiposIva: MutableList<TipoIVA?>?)
        fun onError(errorMessage: String?)
    }
}