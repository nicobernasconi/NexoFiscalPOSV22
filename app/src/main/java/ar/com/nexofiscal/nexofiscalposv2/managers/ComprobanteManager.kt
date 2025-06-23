package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RenglonComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toComprobanteEntityList
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

                // ================== INICIO DE LA MODIFICACIÓN ==================

                val db = AppDatabase.getInstance(context.applicationContext)
                val comprobanteDao = db.comprobanteDao()
                val renglonDao = db.renglonComprobanteDao()
                val gson = Gson()

                // 1. Guardar los comprobantes y crear un mapa de [serverId -> localId]
                // Se insertan uno por uno para obtener el ID local que Room genera.
                val serverToLocalIdMap = mutableMapOf<Int, Long>()
                val comprobanteEntities = finalComprobantes.toComprobanteEntityList()
                comprobanteEntities.forEach { entity ->
                    val localId = comprobanteDao.insert(entity)
                    entity.serverId?.let { serverId ->
                        serverToLocalIdMap[serverId] = localId
                    }
                }

                // 2. Guardar los renglones usando el mapa para asignar el ID local correcto del comprobante.
                val renglonEntities = finalRenglones.mapNotNull { renglon ->
                    val localParentId = serverToLocalIdMap[renglon.comprobanteId]
                    if (localParentId != null) {
                        // Se crea la entidad RenglonComprobanteEntity manualmente
                        RenglonComprobanteEntity(
                            comprobanteLocalId = localParentId.toInt(),
                            data = gson.toJson(renglon)
                        )
                    } else {
                        Log.w(TAG, "No se encontró el comprobante padre local para el renglón con serverId ${renglon.id}")
                        null
                    }
                }
                renglonEntities.forEach { entity -> renglonDao.insert(entity) }

                // =================== FIN DE LA MODIFICACIÓN ====================

                Log.d(TAG, "${comprobanteEntities.size} comprobantes y ${renglonEntities.size} renglones guardados.")

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