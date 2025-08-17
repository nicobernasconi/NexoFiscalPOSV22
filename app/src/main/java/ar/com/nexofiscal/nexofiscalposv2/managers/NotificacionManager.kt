package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.NotificacionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toNotificacionEntityList
import ar.com.nexofiscal.nexofiscalposv2.models.Notificacion
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Headers

object NotificacionManager {
    private const val TAG = "NotificacionManager"
    private const val ENDPOINT = "/api/notificaciones"

    fun obtenerNotificaciones(
        context: Context,
        headers: MutableMap<String?, String?>?,
        callback: NotificacionListCallback
    ) {
        val listType = object : com.google.gson.reflect.TypeToken<MutableList<Notificacion?>?>() {}.type

        ApiClient.request(
            HttpMethod.GET,
            ENDPOINT,
            headers,
            null,
            listType,
            object : ApiCallback<MutableList<Notificacion?>?> {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Headers?,
                    payload: MutableList<Notificacion?>?
                ) {
                    callback.onSuccess(payload)
                    payload?.let { lista ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val dao = AppDatabase.getInstance(context.applicationContext).notificacionDao()
                                val entities = lista.toNotificacionEntityList()
                                // Upsert manual para mantener id local si existe por serverId
                                for (e in entities) {
                                    val toInsert: NotificacionEntity = e.serverId?.let { sid ->
                                        val existente = dao.findByServerId(sid)
                                        if (existente != null) e.copy(id = existente.id) else e
                                    } ?: e
                                    dao.insert(toInsert)
                                }
                                Log.d(TAG, "${entities.size} notificaciones guardadas/actualizadas en la BD.")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al guardar notificaciones: ${e.message}", e)
                            }
                        }
                    }
                }

                override fun onError(statusCode: Int, errorMessage: String?) {
                    Log.e(TAG, "Error al obtener notificaciones. Código: $statusCode, Mensaje: $errorMessage")
                    callback.onError(errorMessage)
                }
            }
        )
    }

    interface NotificacionListCallback {
        fun onSuccess(notificaciones: MutableList<Notificacion?>?)
        fun onError(errorMessage: String?)
    }
}
