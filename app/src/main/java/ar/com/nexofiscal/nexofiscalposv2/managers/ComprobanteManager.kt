package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobantePagoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobantePromocionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RenglonComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.models.Comprobante
import ar.com.nexofiscal.nexofiscalposv2.models.RenglonComprobante
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.Headers
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ComprobanteManager {
    private const val TAG = "ComprobanteManager"
    private const val ENDPOINT_COMPROBANTES = "/api/comprobantes"
    private const val PAGE_SIZE = 255
    private const val TARGET_COUNT = 500

    fun obtenerComprobantes(
        context: Context,
        headers: MutableMap<String?, String?>?,
        sucursalId: Int,
        callback: ComprobanteListCallback
    ) {
        Log.d(TAG, "Descargando los últimos comprobantes por ID.")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allComprobantes = mutableListOf<Comprobante>()
                val allRenglones = mutableListOf<RenglonComprobante>()
                var currentPage = 1
                var keepFetching = true

                while (keepFetching && allComprobantes.size < TARGET_COUNT) {
                    val pageOfComprobantes = fetchComprobantePage(context, headers, sucursalId, currentPage)
                    if (pageOfComprobantes.isNotEmpty()) {
                        allComprobantes.addAll(pageOfComprobantes)
                        val idsOnPage = pageOfComprobantes.map { it.id }.joinToString(",")
                        if (idsOnPage.isNotEmpty()) {
                            val renglonesForPage = fetchRenglonesSuspend(context, idsOnPage, headers)
                            allRenglones.addAll(renglonesForPage)
                        }
                    }
                    if (pageOfComprobantes.size < PAGE_SIZE) {
                        keepFetching = false
                    } else {
                        currentPage++
                    }
                }

                val finalComprobantes = allComprobantes.take(TARGET_COUNT)
                val finalComprobanteIds = finalComprobantes.map { it.id }.toSet()
                val finalRenglones = allRenglones.filter { it.comprobanteId in finalComprobanteIds }

                // ================== LÓGICA DE GUARDADO CORREGIDA ==================
                val db = AppDatabase.getInstance(context.applicationContext)
                val comprobanteDao = db.comprobanteDao()
                val renglonDao = db.renglonComprobanteDao()
                val pagoDao = db.comprobantePagoDao()
                val promocionDao = db.comprobantePromocionDao()
                val gson = Gson()

                // 1. Agrupar los renglones descargados por el ID de su comprobante padre
                val renglonesByComprobanteId = finalRenglones.groupBy { it.comprobanteId }



                // 2. Usar una única transacción para todo el proceso de guardado
                db.runInTransaction {
                    finalComprobantes.forEach { comprobante ->
                        val comprobanteEntity = comprobante.toEntity()
                        val existente = comprobanteEntity.serverId?.let { comprobanteDao.getByServerId(it) }

                        val idFinalParaRenglones: Int

                        if (existente != null) {
                            // --- LÓGICA DE ACTUALIZACIÓN ---
                            idFinalParaRenglones = existente.id
                            renglonDao.deleteByComprobanteId(idFinalParaRenglones)
                            pagoDao.deletePromocionesForComprobante(idFinalParaRenglones) // <-- AÑADIDO
                            promocionDao.deletePromocionesForComprobante(idFinalParaRenglones) // <-- AÑADIDO
                            val entidadParaActualizar = comprobanteEntity.copy(id = idFinalParaRenglones)
                            comprobanteDao.insert(entidadParaActualizar)
                        } else {
                            // --- LÓGICA DE INSERCIÓN ---
                            val nuevoIdLargo = comprobanteDao.insert(comprobanteEntity)
                            idFinalParaRenglones = nuevoIdLargo.toInt()
                        }


                        val nuevosRenglones = renglonesByComprobanteId[comprobante.id] ?: emptyList()
                        val nuevosPagos = comprobante.formas_de_pago ?: emptyList() // <-- AÑADIDO
                        val nuevasPromociones = comprobante.promociones ?: emptyList() // <-- AÑADIDO

                        // --- INICIO DE LA NUEVA CORRECCIÓN ---
                        // Filtramos la lista de renglones nuevos para procesar solo los únicos,
                        // basándonos en su ID único que viene del servidor.
                        val renglonesUnicos = nuevosRenglones.distinctBy { it.id }
                        // --- FIN DE LA NUEVA CORRECCIÓN ---

                        if (renglonesUnicos.isNotEmpty()) {
                            val renglonEntities = renglonesUnicos.map { renglon ->
                                RenglonComprobanteEntity(
                                    comprobanteLocalId = idFinalParaRenglones,
                                    data = gson.toJson(renglon)
                                )
                            }
                            renglonDao.insertAll(renglonEntities)
                        }

                        // Guardar pagos <-- AÑADIDO
                        if (nuevosPagos.isNotEmpty()) {
                            val pagoEntities = nuevosPagos.map { pago ->
                                ComprobantePagoEntity(
                                    comprobanteLocalId = idFinalParaRenglones.toLong(),
                                    formaPagoId = pago.formaPago.id,
                                    importe = pago.monto.toDouble(),
                                    syncStatus = SyncStatus.SYNCED
                                )
                            }
                            pagoDao.insertAll(pagoEntities)
                        }

                        // Guardar promociones <-- AÑADIDO
                        if (nuevasPromociones.isNotEmpty()) {
                            val promocionEntities = nuevasPromociones.map { promocion ->
                                ComprobantePromocionEntity(
                                    comprobanteLocalId = idFinalParaRenglones.toLong(),
                                    promocionId = promocion.id
                                )
                            }
                            promocionDao.insertAll(promocionEntities)
                        }
                    }
                }
                Log.d(TAG, "${finalComprobantes.size} comprobantes y ${finalRenglones.size} renglones procesados.")

                withContext(Dispatchers.Main) {
                    callback.onSuccess(finalComprobantes.toMutableList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fallo al paginar y obtener comprobantes/renglones: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    callback.onError("Error al sincronizar comprobantes: ${e.message}")
                }
            }
        }
    }

    private suspend fun fetchComprobantePage(
        context: Context,
        headers: MutableMap<String?, String?>?,
        sucursalId: Int,
        page: Int
    ): List<Comprobante> = suspendCancellableCoroutine { continuation ->
        val comprobanteListType = object : com.google.gson.reflect.TypeToken<MutableList<Comprobante?>?>() {}.type
        val url = "$ENDPOINT_COMPROBANTES?sucursal_id=$sucursalId&page=$page&limit=$PAGE_SIZE&order_by=id&sort_order=desc"
        ApiClient.request(
            HttpMethod.GET, url, headers, null, comprobanteListType,
            object : ApiCallback<MutableList<Comprobante?>?> {
                override fun onSuccess(statusCode: Int, responseHeaders: Headers?, payload: MutableList<Comprobante?>?) {
                    if (continuation.isActive) continuation.resume(payload?.filterNotNull() ?: emptyList())
                }
                override fun onError(statusCode: Int, errorMessage: String?) {
                    if (continuation.isActive) continuation.resumeWithException(Exception("Error en comprobantes página $page: $errorMessage"))
                }
            }
        )
    }

    private suspend fun fetchRenglonesSuspend(
        context: Context,
        comprobanteIds: String,
        headers: Map<String?, String?>?
    ): List<RenglonComprobante> = suspendCancellableCoroutine { continuation ->
        RenglonComprobanteManager.obtenerRenglonesMasivamente(
            context,
            comprobanteIds,
            headers?.toMutableMap(),
            object : RenglonComprobanteManager.RenglonComprobanteListCallback {
                override fun onSuccess(renglones: MutableList<RenglonComprobante?>?) {
                    if (continuation.isActive) {
                        continuation.resume(renglones?.filterNotNull() ?: emptyList())
                    }
                }
                override fun onError(errorMessage: String?) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(Exception("Error en renglones de Cmps IDs $comprobanteIds: ${errorMessage ?: "Desconocido"}"))
                    }
                }
            })
    }

    interface ComprobanteListCallback {
        fun onSuccess(comprobantes: MutableList<Comprobante?>?)
        fun onError(errorMessage: String?)
    }
}