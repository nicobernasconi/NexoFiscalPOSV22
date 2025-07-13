package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toPromocionEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Promocion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PromocionManager {
    private const val TAG = "PromocionManager"
    private const val ENDPOINT_PROMOCIONES = "/api/promociones"

    fun obtenerPromociones(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: PromocionListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Promocion?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Promocion>(ENDPOINT_PROMOCIONES, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).promocionDao()
                val entities = allItems.toPromocionEntityList()
                dao.upsertAll(entities)
                Log.d(TAG, "${entities.size} promociones guardadas/actualizadas en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de promociones: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface PromocionListCallback {
        fun onSuccess(promociones: MutableList<Promocion?>?)
        fun onError(errorMessage: String?)
    }
}