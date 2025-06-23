package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toClienteEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ClienteManager {
    private const val TAG = "ClienteManager"
    private const val ENDPOINT_CLIENTES = "/api/clientes"

    fun obtenerClientes(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: ClienteListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val clienteListType = object : com.google.gson.reflect.TypeToken<MutableList<Cliente?>?>() {}.type

                // 1. Llama al gestor de paginación para obtener TODOS los clientes
                val allClientes = PaginationManager.fetchAllPages<Cliente>(ENDPOINT_CLIENTES, headers, clienteListType)

                // 2. Guarda la lista completa en la base de datos
                val clienteDao = AppDatabase.getInstance(context.applicationContext).clienteDao()
                val clienteEntities = allClientes.toClienteEntityList()
                clienteEntities.forEach { entity ->
                    clienteDao.insert(entity)
                }
                Log.d(TAG, "${clienteEntities.size} clientes guardados/actualizados en la BD.")

                // 3. Notifica el éxito con la lista completa
                withContext(Dispatchers.Main) {
                    callback.onSuccess(allClientes.toMutableList())
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener todas las páginas de clientes: ", e)
                withContext(Dispatchers.Main) {
                    callback.onError(e.message)
                }
            }
        }
    }

    interface ClienteListCallback {
        fun onSuccess(clientes: MutableList<Cliente?>?)
        fun onError(errorMessage: String?)
    }
}