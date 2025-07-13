package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toTasaIvaEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.TasaIva
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TasaIvaManager {
    private const val TAG = "TasaIvaManager"
    private const val ENDPOINT_TASAS_IVA = "/api/tasa_iva"

    fun obtenerTasasIva(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: TasaIvaListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<TasaIva?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<TasaIva>(ENDPOINT_TASAS_IVA, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).tasaIvaDao()
                val entities = allItems.toTasaIvaEntityList()
                dao.upsertAll(entities)
                Log.d(TAG, "${entities.size} tasas de IVA guardadas/actualizadas en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de tasas de IVA: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface TasaIvaListCallback {
        fun onSuccess(tasasIva: MutableList<TasaIva?>?)
        fun onError(errorMessage: String?)
    }
}