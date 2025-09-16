package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockActualizacionEntity
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Headers
import org.json.JSONObject
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object StockActualizacionManager {
    private const val TAG = "StockActualizacionManager"
    private const val ENDPOINT_STOCK_UPDATE = "/api/stocks"

    private val envioEnProgreso = AtomicBoolean(false)

    /**
     * Registra una actualización de stock pendiente de envío
     */
    fun registrarActualizacionStock(
        context: Context,
        productoId: Int,
        sucursalId: Int,
        cantidad: Double
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(context)
                val stockActualizacionDao = database.stockActualizacionDao()

                stockActualizacionDao.upsertActualizacion(productoId, sucursalId, cantidad)

                Log.d(TAG, "Actualización de stock registrada: Producto=$productoId Sucursal=$sucursalId Cantidad=$cantidad")
            } catch (e: Exception) {
                Log.e(TAG, "Error al registrar actualización de stock", e)
            }
        }
    }

    /**
     * Envía todas las actualizaciones pendientes al servidor (sincrónico/suspend)
     */
    suspend fun enviarActualizacionesPendientes(
        context: Context,
        headers: MutableMap<String?, String?>?
    ) = withContext(Dispatchers.IO) {
        if (!envioEnProgreso.compareAndSet(false, true)) {
            Log.d(TAG, "Envío ya en progreso. Se omite nueva ejecución simultánea.")
            return@withContext
        }
        Log.d(TAG, "-> INICIANDO enviarActualizacionesPendientes [${Thread.currentThread().name}]")
        try {
            val database = AppDatabase.getInstance(context)
            val stockActualizacionDao = database.stockActualizacionDao()

            val pendientes = stockActualizacionDao.getPendientesDeEnvio()

            if (pendientes.isEmpty()) {
                Log.d(TAG, "No hay actualizaciones de stock pendientes")
                return@withContext
            }

            Log.d(TAG, "Enviando ${pendientes.size} actualizaciones de stock pendientes")

            for (actualizacion in pendientes) {
                try {
                    Log.d(TAG, "Procesando actualización idLocal=${actualizacion.id}")
                    enviarActualizacionIndividual(context, actualizacion, headers)
                    Log.d(TAG, "Actualización idLocal=${actualizacion.id} completada")
                } catch (e: Exception) {
                    Log.e(TAG, "Fallo envío para idLocal=${actualizacion.id}", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar actualizaciones pendientes", e)
        } finally {
            envioEnProgreso.set(false)
            Log.d(TAG, "<- FINALIZANDO enviarActualizacionesPendientes [${Thread.currentThread().name}]")
        }
    }

    /**
     * Envía una actualización individual al servidor (suspend)
     */
    private data class StockUpdateResponse(
        val status: Int?,
        val status_message: String?,
        val id: Long?
    )

    private suspend fun enviarActualizacionIndividual(
        context: Context,
        actualizacion: StockActualizacionEntity,
        headers: MutableMap<String?, String?>?
    ) = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getInstance(context)
            val stockActualizacionDao = database.stockActualizacionDao()
            val productoDao = database.productoDao()

            // Traducir id local -> serverId
            val producto = productoDao.getById(actualizacion.productoId)
            val serverId = producto?.serverId
            if (serverId == null) {
                Log.e(
                    TAG,
                    "No se envía actualización idLocal=${actualizacion.id}: producto localId=${actualizacion.productoId} sin serverId (posible no sincronizado)."
                )
                // Registrar intento fallido para que se pueda reintentar luego de sincronizar productos
                stockActualizacionDao.incrementarIntentos(actualizacion.id, "Producto sin serverId")
                return@withContext
            }

            val requestJsonObject = JSONObject().apply {
                put("producto_id", serverId) // usar serverId real en el payload
                put("sucursal_id", actualizacion.sucursalId)
                put("cantidad", actualizacion.cantidad)
            }

            val safeHeaders = headers?.mapValues { (k, v) ->
                if (k.equals("Authorization", true) && !v.isNullOrBlank()) {
                    val token = v
                    if (token.length > 15) token.substring(0, 10) + "..." + token.takeLast(5) else "***"
                } else v
            }

            val inicio = System.currentTimeMillis()
            Log.d(TAG, buildString {
                append("[PRE-REQUEST] idLocal=${actualizacion.id} -> POST $ENDPOINT_STOCK_UPDATE\n")
                append("Mapeo producto local=${actualizacion.productoId} -> serverId=$serverId\n")
                append("Payload: ${requestJsonObject}\n")
                append("Headers: $safeHeaders\n")
                append("Intentos previos: ${actualizacion.intentos} Enviado=${actualizacion.enviado}")
            })

            val responseType = object : TypeToken<StockUpdateResponse?>() {}.type

            val result = suspendCancellableCoroutine<Pair<Int, StockUpdateResponse?>> { continuation ->
                ApiClient.request(
                    HttpMethod.POST,
                    ENDPOINT_STOCK_UPDATE,
                    headers,
                    requestJsonObject,
                    responseType,
                    object : ApiCallback<StockUpdateResponse?> {
                        override fun onSuccess(
                            statusCode: Int,
                            headers: Headers?,
                            payload: StockUpdateResponse?
                        ) {
                            if (continuation.isActive) continuation.resume(Pair(statusCode, payload))
                        }

                        override fun onError(statusCode: Int, errorMessage: String?) {
                            if (continuation.isActive) continuation.resumeWithException(Exception("Error en API ($statusCode): $errorMessage"))
                        }
                    }
                )
            }

            // Marcar como enviado de forma sincrónica tras éxito
            stockActualizacionDao.marcarComoEnviado(actualizacion.id)
            val duracion = System.currentTimeMillis() - inicio
            Log.i(
                TAG,
                "[OK] idLocal=${actualizacion.id} productoLocal=${actualizacion.productoId} serverId=$serverId HTTP=${result.first} Duración=${duracion}ms status_api=${result.second?.status} msg='${result.second?.status_message}' nuevoId=${result.second?.id}"
            )
        } catch (e: Exception) {
            val database = AppDatabase.getInstance(context)
            val stockActualizacionDao = database.stockActualizacionDao()
            try {
                stockActualizacionDao.incrementarIntentos(
                    actualizacion.id,
                    e.message ?: "Error desconocido"
                )
            } catch (_: Exception) {}
            val duracion = System.currentTimeMillis()
            Log.e(
                TAG,
                "[ERROR] idLocal=${actualizacion.id} productoLocal=${actualizacion.productoId} error='${e.message ?: "(sin detalle)"}' Duración=${duracion}ms payload_pendiente"
            )
            throw e
        }
    }

    /**
     * Obtiene el conteo de actualizaciones pendientes
     */
    fun obtenerContadorPendientes(context: Context, callback: (Int) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(context)
                val stockActualizacionDao = database.stockActualizacionDao()
                val pendientes = stockActualizacionDao.getPendientesDeEnvio()

                CoroutineScope(Dispatchers.Main).launch { callback(pendientes.size) }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener contador de pendientes", e)
                CoroutineScope(Dispatchers.Main).launch { callback(0) }
            }
        }
    }

    /**
     * Limpia actualizaciones enviadas antiguas (más de 7 días)
     */
    fun limpiarActualizacionesAntiguas(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(context)
                val stockActualizacionDao = database.stockActualizacionDao()

                val fechaLimite = Date(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)) // 7 días atrás
                stockActualizacionDao.limpiarEnviadosAntiguos(fechaLimite)

                Log.d(TAG, "Actualizaciones antiguas limpiadas")
            } catch (e: Exception) {
                Log.e(TAG, "Error al limpiar actualizaciones antiguas", e)
            }
        }
    }
}
