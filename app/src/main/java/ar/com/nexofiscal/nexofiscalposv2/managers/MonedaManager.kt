package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toMonedaEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Moneda
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object MonedaManager {
    private const val TAG = "MonedaManager"
    private const val ENDPOINT_MONEDAS = "/api/monedas"

    fun obtenerMonedas(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: MonedaListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Moneda?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Moneda>(ENDPOINT_MONEDAS, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).monedaDao()
                val entities = allItems.toMonedaEntityList()
                entities.forEach { entity -> dao.insert(entity) }
                Log.d(TAG, "${entities.size} monedas guardadas/actualizadas en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de monedas: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface MonedaListCallback {
        fun onSuccess(monedas: MutableList<Moneda?>?)
        fun onError(errorMessage: String?)
    }
}