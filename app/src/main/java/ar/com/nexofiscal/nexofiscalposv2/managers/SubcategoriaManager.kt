package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toSubcategoriaEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Subcategoria
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Headers

object SubcategoriaManager {
    private const val TAG = "SubcategoriaManager"
    private const val ENDPOINT_SUBCATEGORIAS = "/api/subcategorias"

    fun obtenerSubcategorias(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: SubcategoriaListCallback
    ) {
        val subcategoriaListType = object :
            com.google.gson.reflect.TypeToken<MutableList<Subcategoria?>?>() {}.type

        ApiClient.request(
            HttpMethod.GET,
            ENDPOINT_SUBCATEGORIAS,
            headers,
            null,
            subcategoriaListType,
            object : ApiCallback<MutableList<Subcategoria?>?> {
                override fun onSuccess(
                    statusCode: Int,
                    responseHeaders: Headers?,
                    subcategorias: MutableList<Subcategoria?>?
                ) {
                    callback.onSuccess(subcategorias)

                    subcategorias?.let { listaSubcategorias ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val subcategoriaDao = AppDatabase.getInstance(context.applicationContext).subcategoriaDao()
                                val subcategoriaEntities = listaSubcategorias.toSubcategoriaEntityList()
                                subcategoriaEntities.forEach { entity ->
                                    subcategoriaDao.insert(entity)
                                }
                                Log.d(TAG, "${subcategoriaEntities.size} subcategorías guardadas/actualizadas en la BD.")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al guardar subcategorías en la BD: ${e.message}", e)
                            }
                        }
                    }
                }

                override fun onError(statusCode: Int, errorMessage: String?) {
                    Log.e(
                        TAG,
                        "Error al obtener subcategorías. Código: $statusCode, Mensaje: $errorMessage"
                    )
                    callback.onError(errorMessage)
                }
            }
        )
    }

    interface SubcategoriaListCallback {
        fun onSuccess(subcategorias: MutableList<Subcategoria?>?)
        fun onError(errorMessage: String?)
    }
}