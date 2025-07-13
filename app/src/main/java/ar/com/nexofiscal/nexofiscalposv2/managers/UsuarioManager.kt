package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toUsuarioEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object UsuarioManager {
    private const val TAG = "UsuarioManager"
    private const val ENDPOINT_USUARIOS = "/api/usuarios"

    fun obtenerUsuarios(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: UsuarioListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Usuario?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Usuario>(ENDPOINT_USUARIOS, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).usuarioDao()
                val entities = allItems.toUsuarioEntityList()
                dao.upsertAll(entities)
                Log.d(TAG, "${entities.size} usuarios guardados/actualizados en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de usuarios: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface UsuarioListCallback {
        fun onSuccess(usuarios: MutableList<Usuario?>?)
        fun onError(errorMessage: String?)
    }
}