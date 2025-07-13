package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toRolEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Rol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object RolManager {
    private const val TAG = "RolManager"
    private const val ENDPOINT_ROLES = "/api/roles"

    fun obtenerRoles(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: RolListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<MutableList<Rol?>?>() {}.type
                val allItems = PaginationManager.fetchAllPages<Rol>(ENDPOINT_ROLES, headers, listType)

                val dao = AppDatabase.getInstance(context.applicationContext).rolDao()
                val entities = allItems.toRolEntityList()
                dao.upsertAll(entities)
                Log.d(TAG, "${entities.size} roles guardados/actualizados en la BD.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(allItems.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de roles: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface RolListCallback {
        fun onSuccess(roles: MutableList<Rol?>?)
        fun onError(errorMessage: String?)
    }
}