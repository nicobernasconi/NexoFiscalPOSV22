package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockActualizacionEntity
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Headers
import org.json.JSONObject
import java.util.Date

object StockActualizacionManager {
    private const val TAG = "StockActualizacionManager"
    private const val ENDPOINT_STOCK_UPDATE = "/api/stocks"

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

                Log.d(TAG, "Actualización de stock registrada: Producto $productoId, Sucursal $sucursalId, Cantidad $cantidad")
            } catch (e: Exception) {
                Log.e(TAG, "Error al registrar actualización de stock", e)
            }
        }
    }

    /**
     * Envía todas las actualizaciones pendientes al servidor
     */
    fun enviarActualizacionesPendientes(
        context: Context,
        headers: MutableMap<String?, String?>?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(context)
                val stockActualizacionDao = database.stockActualizacionDao()

                val pendientes = stockActualizacionDao.getPendientesDeEnvio()

                if (pendientes.isEmpty()) {
                    Log.d(TAG, "No hay actualizaciones de stock pendientes")
                    return@launch
                }

                Log.d(TAG, "Enviando ${pendientes.size} actualizaciones de stock pendientes")

                pendientes.forEach { actualizacion ->
                    enviarActualizacionIndividual(context, actualizacion, headers)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar actualizaciones pendientes", e)
            }
        }
    }

    /**
     * Envía una actualización individual al servidor
     */
    private suspend fun enviarActualizacionIndividual(
        context: Context,
        actualizacion: StockActualizacionEntity,
        headers: MutableMap<String?, String?>?
    ) {
        try {
            val database = AppDatabase.getInstance(context)
            val stockActualizacionDao = database.stockActualizacionDao()

            val requestBody = JSONObject().apply {
                put("producto_id", actualizacion.productoId)
                put("sucursal_id", actualizacion.sucursalId)
                put("cantidad", actualizacion.cantidad)
            }.toString()

            val responseType = object : com.google.gson.reflect.TypeToken<String?>() {}.type

            ApiClient.request(
                HttpMethod.POST,
                ENDPOINT_STOCK_UPDATE,
                headers,
                requestBody,
                responseType,
                object : ApiCallback<String?> {
                    override fun onSuccess(
                        statusCode: Int,
                        responseHeaders: Headers?,
                        response: String?
                    ) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                stockActualizacionDao.marcarComoEnviado(actualizacion.id)
                                Log.d(TAG, "Actualización enviada exitosamente: ${actualizacion.id}")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al marcar como enviado", e)
                            }
                        }
                    }

                    override fun onError(statusCode: Int, errorMessage: String?) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                stockActualizacionDao.incrementarIntentos(
                                    actualizacion.id,
                                    errorMessage ?: "Error $statusCode"
                                )
                                Log.e(TAG, "Error al enviar actualización ${actualizacion.id}: $errorMessage")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al incrementar intentos", e)
                            }
                        }
                    }
                }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar actualización individual", e)
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

                CoroutineScope(Dispatchers.Main).launch {
                    callback(pendientes.size)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener contador de pendientes", e)
                CoroutineScope(Dispatchers.Main).launch {
                    callback(0)
                }
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
