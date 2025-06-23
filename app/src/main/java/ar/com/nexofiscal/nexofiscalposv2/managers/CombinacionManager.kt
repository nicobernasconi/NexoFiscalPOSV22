package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toCombinacionEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Combinacion
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Headers

object CombinacionManager {
    private const val TAG = "CombinacionManager"
    // Endpoint puede ser más complejo si depende de un producto, ej: /api/productos/{id}/combinaciones
    // Por ahora, usamos un endpoint general.
    private const val ENDPOINT_COMBINACIONES = "/api/combinaciones"

    fun obtenerCombinaciones(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: CombinacionListCallback
        // Podrías añadir productoId si el endpoint lo requiere: productoId: String,
    ) {
        val combinacionListType = object :
            com.google.gson.reflect.TypeToken<MutableList<Combinacion?>?>() {}.type

        // Ajustar la URL si es un sub-recurso:
        // val url = "$ENDPOINT_PRODUCTOS/$productoId/combinaciones"
        val url = ENDPOINT_COMBINACIONES

        ApiClient.request(
            HttpMethod.GET,
            url,
            headers,
            null,
            combinacionListType,
            object : ApiCallback<MutableList<Combinacion?>?> {
                override fun onSuccess(
                    statusCode: Int,
                    responseHeaders: Headers?,
                    combinaciones: MutableList<Combinacion?>?
                ) {
                    callback.onSuccess(combinaciones)

                    combinaciones?.let { listaCombinaciones ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val combinacionDao = AppDatabase.getInstance(context.applicationContext).combinacionDao()
                                val combinacionEntities = listaCombinaciones.toCombinacionEntityList()
                                combinacionEntities.forEach { entity ->
                                    combinacionDao.insert(entity)
                                }
                                Log.d(TAG, "${combinacionEntities.size} combinaciones guardadas/actualizadas en la BD.")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al guardar combinaciones en la BD: ${e.message}", e)
                            }
                        }
                    }
                }

                override fun onError(statusCode: Int, errorMessage: String?) {
                    Log.e(
                        TAG,
                        "Error al obtener combinaciones. Código: $statusCode, Mensaje: $errorMessage"
                    )
                    callback.onError(errorMessage)
                }
            }
        )
    }

    interface CombinacionListCallback {
        fun onSuccess(combinaciones: MutableList<Combinacion?>?)
        fun onError(errorMessage: String?)
    }
}