package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toDomainModel
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toUploadRequest
import ar.com.nexofiscal.nexofiscalposv2.models.*
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Headers
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


object UploadManager {

    private const val TAG = "UploadManager"
    private val gson = Gson()

    suspend fun uploadLocalChanges(context: Context, token: String) {
        val db = AppDatabase.getInstance(context)
        val headers = mutableMapOf<String?, String?>("Authorization" to "Bearer $token")

        withContext(Dispatchers.IO) {
            Log.d(TAG, "================================================")
            Log.d(TAG, "INICIANDO PROCESO DE SUBIDA DE CAMBIOS LOCALES")
            Log.d(TAG, "================================================")

            // --- INICIO DE LA MODIFICACIÓN: Se reordena la subida por dependencias ---
            // 1. Entidades independientes o de primer nivel
            uploadFamilias(db, headers)
            uploadPromociones(db, headers)
            uploadFormasDePago(db, headers) // Asumiendo que TipoFormaPago no se crea localmente

            // 2. Entidades con dependencias de nivel 1
            uploadProveedores(db, headers) // Depende de Localidad, etc. (se omite chequeo si no se gestionan localmente)
            uploadClientes(db, headers)    // Depende de Localidad, etc.

            // 3. Entidades con dependencias de nivel 2
            uploadProductos(db, headers)   // Depende de Familia, Proveedor

            // 4. Entidades de transacciones (las últimas)
            uploadComprobantes(db, headers)// Depende de Cliente, Producto
            // --- FIN DE LA MODIFICACIÓN ---

            Log.d(TAG, "================================================")
            Log.d(TAG, "PROCESO DE SUBIDA FINALIZADO")
            Log.d(TAG, "================================================")
        }
    }

    private suspend fun uploadClientes(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.clienteDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) return
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Clientes] ---")
            for (entity in unsyncedItems) {
                try {
                    // Aquí se podrían añadir chequeos de dependencias si Localidad, etc., se gestionan localmente
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val domainModel = entity.toDomainModel()
                            Log.d(TAG, "[CLIENTE] Petición POST a /api/clientes. Body: ${gson.toJson(domainModel)}")
                            val response = apiRequest<Cliente>(HttpMethod.POST, "/api/clientes", headers, domainModel, object : TypeToken<Cliente>() {}.type)
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[CLIENTE] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val domainModel = entity.toDomainModel()
                            Log.d(TAG, "[CLIENTE] Petición PUT a /api/clientes/${entity.serverId}. Body: ${gson.toJson(domainModel)}")
                            apiRequest<Unit>(HttpMethod.PUT, "/api/clientes/${entity.serverId}", headers, domainModel, object : TypeToken<Unit>() {}.type)
                            dao.updateStatusToSyncedByServerId(entity.serverId!!)
                            Log.i(TAG, "[CLIENTE] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[CLIENTE] Falló subida para LocalID ${entity.id} (ServerID: ${entity.serverId}). Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadClientes: ${e.message}", e)
        }
    }

    private suspend fun uploadProveedores(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.proveedorDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) return
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Proveedores] ---")
            for (entity in unsyncedItems) {
                try {
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val domainModel = entity.toDomainModel()
                            Log.d(TAG, "[PROVEEDOR] Petición POST a /api/proveedores. Body: ${gson.toJson(domainModel)}")
                            val response = apiRequest<Proveedor>(HttpMethod.POST, "/api/proveedores", headers, domainModel, object : TypeToken<Proveedor>() {}.type)
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[PROVEEDOR] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val domainModel = entity.toDomainModel()
                            Log.d(TAG, "[PROVEEDOR] Petición PUT a /api/proveedores/${entity.serverId}. Body: ${gson.toJson(domainModel)}")
                            apiRequest<Unit>(HttpMethod.PUT, "/api/proveedores/${entity.serverId}", headers, domainModel, object : TypeToken<Unit>() {}.type)
                            dao.updateStatusToSyncedByServerId(entity.serverId!!)
                            Log.i(TAG, "[PROVEEDOR] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[PROVEEDOR] Falló subida para LocalID ${entity.id} (ServerID: ${entity.serverId}). Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadProveedores: ${e.message}", e)
        }
    }

    private suspend fun uploadProductos(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.productoDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) return
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Productos] ---")
            for (entity in unsyncedItems) {
                try {
                    // --- INICIO DE LA VERIFICACIÓN DE DEPENDENCIAS ---
                    if (entity.proveedorId != null && db.proveedorDao().findByServerId(entity.proveedorId) == null) {
                        Log.w(TAG, "[PRODUCTO] Omitiendo subida para LocalID ${entity.id}. El Proveedor con ServerID ${entity.proveedorId} no está sincronizado.")
                        continue
                    }
                    if (entity.familiaId != null && db.familiaDao().findByServerId(entity.familiaId) == null) {
                        Log.w(TAG, "[PRODUCTO] Omitiendo subida para LocalID ${entity.id}. La Familia con ServerID ${entity.familiaId} no está sincronizada.")
                        continue
                    }
                    // ... agregar más chequeos para otras dependencias si es necesario (tasaIvaId, etc.)
                    // --- FIN DE LA VERIFICACIÓN DE DEPENDENCIAS ---

                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val domainModel = entity.toDomainModel()
                            Log.d(TAG, "[PRODUCTO] Petición POST a /api/productos. Body: ${gson.toJson(domainModel)}")
                            val response = apiRequest<Producto>(HttpMethod.POST, "/api/productos", headers, domainModel, object : TypeToken<Producto>() {}.type)
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[PRODUCTO] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val domainModel = entity.toDomainModel()
                            Log.d(TAG, "[PRODUCTO] Petición PUT a /api/productos/${entity.serverId}. Body: ${gson.toJson(domainModel)}")
                            apiRequest<Unit>(HttpMethod.PUT, "/api/productos/${entity.serverId}", headers, domainModel, object : TypeToken<Unit>() {}.type)
                            dao.updateStatusToSyncedByServerId(entity.serverId!!)
                            Log.i(TAG, "[PRODUCTO] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[PRODUCTO] Falló subida para LocalID ${entity.id} (ServerID: ${entity.serverId}). Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadProductos: ${e.message}", e)
        }
    }

    private suspend fun uploadFamilias(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.familiaDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) return
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Familias] ---")
            for (entity in unsyncedItems) {
                try {
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val domainModel = entity.toDomainModel()
                            Log.d(TAG, "[FAMILIA] Petición POST a /api/familias. Body: ${gson.toJson(domainModel)}")
                            val response = apiRequest<Familia>(HttpMethod.POST, "/api/familias", headers, domainModel, object : TypeToken<Familia>() {}.type)
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[FAMILIA] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val domainModel = entity.toDomainModel()
                            Log.d(TAG, "[FAMILIA] Petición PUT a /api/familias/${entity.serverId}. Body: ${gson.toJson(domainModel)}")
                            apiRequest<Unit>(HttpMethod.PUT, "/api/familias/${entity.serverId}", headers, domainModel, object : TypeToken<Unit>() {}.type)
                            dao.updateStatusToSyncedByServerId(entity.serverId!!)
                            Log.i(TAG, "[FAMILIA] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[FAMILIA] Falló subida para LocalID ${entity.id} (ServerID: ${entity.serverId}). Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadFamilias: ${e.message}", e)
        }
    }

    private suspend fun uploadFormasDePago(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.formaPagoDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) return
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Formas de Pago] ---")
            for (entity in unsyncedItems) {
                try {
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val domainModel = entity.toDomainModel()
                            Log.d(TAG, "[FORMA_PAGO] Petición POST a /api/formas_pagos. Body: ${gson.toJson(domainModel)}")
                            val response = apiRequest<FormaPago>(HttpMethod.POST, "/api/formas_pagos", headers, domainModel, object : TypeToken<FormaPago>() {}.type)
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[FORMA_PAGO] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val domainModel = entity.toDomainModel()
                            Log.d(TAG, "[FORMA_PAGO] Petición PUT a /api/formas_pagos/${entity.serverId}. Body: ${gson.toJson(domainModel)}")
                            apiRequest<Unit>(HttpMethod.PUT, "/api/formas_pagos/${entity.serverId}", headers, domainModel, object : TypeToken<Unit>() {}.type)
                            dao.updateStatusToSyncedByServerId(entity.serverId!!)
                            Log.i(TAG, "[FORMA_PAGO] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[FORMA_PAGO] Falló subida para LocalID ${entity.id} (ServerID: ${entity.serverId}). Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadFormasDePago: ${e.message}", e)
        }
    }

    private suspend fun uploadPromociones(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.promocionDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) return
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Promociones] ---")
            for (entity in unsyncedItems) {
                try {
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val domainModel = entity.toDomainModel()
                            Log.d(TAG, "[PROMOCION] Petición POST a /api/promociones. Body: ${gson.toJson(domainModel)}")
                            val response = apiRequest<Promocion>(HttpMethod.POST, "/api/promociones", headers, domainModel, object : TypeToken<Promocion>() {}.type)
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[PROMOCION] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val domainModel = entity.toDomainModel()
                            Log.d(TAG, "[PROMOCION] Petición PUT a /api/promociones/${entity.serverId}. Body: ${gson.toJson(domainModel)}")
                            apiRequest<Unit>(HttpMethod.PUT, "/api/promociones/${entity.serverId}", headers, domainModel, object : TypeToken<Unit>() {}.type)
                            dao.updateStatusToSyncedByServerId(entity.serverId!!)
                            Log.i(TAG, "[PROMOCION] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[PROMOCION] Falló subida para LocalID ${entity.id} (ServerID: ${entity.serverId}). Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadPromociones: ${e.message}", e)
        }
    }

    private suspend fun uploadComprobantes(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val comprobanteDao = db.comprobanteDao()
        val renglonDao = db.renglonComprobanteDao()

        try {
            val unsyncedItems = comprobanteDao.getUnsynced()
            if (unsyncedItems.isEmpty()) return
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Comprobantes] ---")

            for (entity in unsyncedItems) {
                try {
                    // Verificación de dependencias (ej. cliente debe estar sincronizado)
                    if (db.clienteDao().findByServerId(entity.clienteId) == null && entity.clienteId != 1) {
                        Log.w(TAG, "[COMPROBANTE] Omitiendo subida para LocalID ${entity.id}. El Cliente con ServerID ${entity.clienteId} no está sincronizado.")
                        continue
                    }

                    if (entity.syncStatus == SyncStatus.CREATED) {
                        // --- INICIO DEL FLUJO DE 2 PASOS ---

                        // PASO 1: SUBIR LA CABEZA DEL COMPROBANTE
                        val comprobanteDomain = entity.toDomainModel()
                        val comprobanteUploadRequest = comprobanteDomain.toUploadRequest()
                        Log.d(TAG, "[COMPROBANTE] Subiendo cabeza... LocalID=${entity.id}. Body: ${gson.toJson(comprobanteUploadRequest)}")

                        val responseComprobante = apiRequest<Comprobante>(
                            method = HttpMethod.POST,
                            url = "/api/comprobantes/",
                            headers = headers,
                            body = comprobanteUploadRequest,
                            responseType = object : TypeToken<Comprobante>() {}.type
                        )
                        val newServerId = responseComprobante.id
                        Log.i(TAG, "[COMPROBANTE] Éxito. LocalID ${entity.id} -> ServerID $newServerId.")

                        // PASO 2: SUBIR LOS RENGLONES UNO POR UNO
                        val renglonEntities = renglonDao.getByComprobante(entity.id).first()
                        val renglonesDomain = renglonEntities.map { it.toDomainModel() }
                        var renglonesExitosos = 0

                        Log.d(TAG, "[RENGLONES] Subiendo ${renglonesDomain.size} renglones para Comprobante ServerID $newServerId...")
                        for (renglon in renglonesDomain) {
                            try {
                                val renglonUploadRequest = renglon.toUploadRequest(comprobanteServerId = newServerId)
                                apiRequest<Unit>(
                                    method = HttpMethod.POST,
                                    url = "/api/renglones_comprobantes/$newServerId/", // Endpoint para renglones
                                    headers = headers,
                                    body = renglonUploadRequest,
                                    responseType = object : TypeToken<Unit>() {}.type
                                )
                                renglonesExitosos++
                            } catch (renglonError: Exception) {
                                Log.e(TAG, "[RENGLON] Falló subida de renglón para Comprobante ServerID $newServerId. Error: ${renglonError.message}")
                            }
                        }
                        Log.i(TAG, "[RENGLONES] Subida finalizada: ${renglonesExitosos}/${renglonesDomain.size} renglones exitosos.")

                        // PASO 3: ACTUALIZAR ESTADO LOCAL SI TODO FUE EXITOSO
                        if (renglonesExitosos == renglonesDomain.size) {
                            comprobanteDao.updateServerIdAndStatus(entity.id, newServerId)
                            Log.i(TAG, "[COMPROBANTE] Sincronización completa para Comprobante LocalID ${entity.id}.")
                        } else {
                            Log.w(TAG, "[COMPROBANTE] Sincronización incompleta para LocalID ${entity.id}. No se marcará como SYNCED.")
                        }

                    } else {
                        Log.w(TAG, "[COMPROBANTE] Se omite la subida para el estado ${entity.syncStatus} del comprobante con LocalID ${entity.id}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[COMPROBANTE] Falló subida para LocalID ${entity.id}. Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadComprobantes: ${e.message}", e)
        }
    }

    private suspend fun <T> apiRequest(
        method: HttpMethod,
        url: String,
        headers: MutableMap<String?, String?>,
        body: Any? = null,
        responseType: Type
    ): T = suspendCancellableCoroutine { continuation ->
        ApiClient.request(method, url, headers, body, responseType, object : ApiCallback<T?> {
            override fun onSuccess(statusCode: Int, responseHeaders: Headers?, payload: T?) {
                if (continuation.isActive) {
                    if (payload != null) {
                        continuation.resume(payload)
                    } else {
                        if (statusCode in 200..299) {
                            @Suppress("UNCHECKED_CAST")
                            continuation.resume(Unit as T)
                        } else {
                            continuation.resumeWithException(Exception("Payload nulo con código de error $statusCode"))
                        }
                    }
                }
            }

            override fun onError(statusCode: Int, errorMessage: String?) {
                if (continuation.isActive) {
                    continuation.resumeWithException(Exception("Error en API ($statusCode): $errorMessage"))
                }
            }
        })
    }
}