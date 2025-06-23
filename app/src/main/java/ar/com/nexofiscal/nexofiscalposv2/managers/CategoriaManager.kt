package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toCategoriaEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Categoria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CategoriaManager {
    private const val TAG = "CategoriaManager"
    private const val ENDPOINT_CATEGORIAS = "/api/categorias"

    fun obtenerCategorias(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: CategoriaListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Categoria?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Categoria>(ENDPOINT_CATEGORIAS, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).categoriaDao()
                val entities = allItems.toCategoriaEntityList()
                entities.forEach { entity -> dao.insert(entity) }
                Log.d(TAG, "${entities.size} categorías guardadas/actualizadas en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de categorías: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface CategoriaListCallback {
        fun onSuccess(categorias: MutableList<Categoria?>?)
        fun onError(errorMessage: String?)
    }
}