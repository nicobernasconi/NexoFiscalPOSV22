package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toSucursalEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Sucursal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SucursalManager {
    private const val TAG = "SucursalManager"
    private const val ENDPOINT_SUCURSALES = "/api/sucursales"

    fun obtenerSucursales(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: SucursalListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Sucursal?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Sucursal>(ENDPOINT_SUCURSALES, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).sucursalDao()
                val entities = allItems.toSucursalEntityList()
                dao.upsertAll(entities)
                Log.d(TAG, "${entities.size} sucursales guardadas/actualizadas en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de sucursales: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface SucursalListCallback {
        fun onSuccess(sucursales: MutableList<Sucursal?>?)
        fun onError(errorMessage: String?)
    }
}