package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.content.Intent
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.SyncService
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
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object UploadManager {

    private const val TAG = "UploadManager"
    private val gson = Gson()
    /**
     * Inicia el SyncService en segundo plano para ejecutar una única
     * subida de datos locales.
     *
     * @param context El contexto de la aplicación.
     */
    fun triggerImmediateUpload(context: Context) {
        val intent = Intent(context, SyncService::class.java).apply {
            action = SyncService.ACTION_TRIGGER_UPLOAD_ONCE
        }
        context.startService(intent)
        Log.i(TAG, "Se ha solicitado una subida inmediata de datos.")
    }

    suspend fun uploadLocalChanges(context: Context, token: String) {
        val db = AppDatabase.getInstance(context)
        val headers = mutableMapOf<String?, String?>("Authorization" to "Bearer $token")

        withContext(Dispatchers.IO) {
            Log.d(TAG, "================================================")
            Log.d(TAG, "INICIANDO PROCESO DE SUBIDA DE CAMBIOS LOCALES")
            Log.d(TAG, "================================================")

            // --- Orden de subida por dependencias ---

            uploadAgrupaciones(db, headers)
            uploadCategorias(db, headers)
            uploadFamilias(db, headers)
            uploadClientes(db, headers)
            uploadFormasDePago(db, headers)
            uploadMonedas(db, headers)
            uploadPromociones(db, headers)
            uploadTipoDocumento(db, headers)
            uploadTipoIVA(db, headers)
            uploadUnidades(db, headers)
            uploadProveedores(db, headers)
            uploadProductos(db, headers)
            uploadComprobantes(db, headers)

            Log.d(TAG, "================================================")
            Log.d(TAG, "PROCESO DE SUBIDA FINALIZADO")
            Log.d(TAG, "================================================")
        }
    }
    private suspend fun uploadAgrupaciones(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.agrupacionDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) {
                return
            }
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Agrupaciones] ---")

            for (entity in unsyncedItems) {
                try {
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            Log.d(TAG, "[AGRUPACION] Petición POST. Body: ${gson.toJson(uploadRequest)}")

                            // En la creación, esperamos un objeto Agrupacion de vuelta.
                            val response = apiRequest<Agrupacion>(
                                method = HttpMethod.POST,
                                url = "/api/agrupaciones",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Agrupacion>() {}.type // Se especifica el tipo
                            )

                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[AGRUPACION] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            Log.d(TAG, "[AGRUPACION] Petición PUT a /api/agrupaciones/${entity.serverId}. Body: ${gson.toJson(uploadRequest)}")

                            // En la actualización (PUT), no esperamos cuerpo de respuesta, solo éxito.
                            // Especificamos 'Unit' como el tipo de respuesta.
                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/agrupaciones/${entity.serverId}",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type // ¡CORRECCIÓN CLAVE!
                            )

                            dao.updateStatusToSyncedByServerId(entity.serverId!!)
                            Log.i(TAG, "[AGRUPACION] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[AGRUPACION] Falló subida para LocalID ${entity.id} (ServerID: ${entity.serverId}). Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadAgrupaciones: ${e.message}", e)
        }
    }

    private suspend fun uploadCategorias(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.categoriaDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) {
                return // No hay nada que subir, termina la función.
            }
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Categorías] ---")

            for (entity in unsyncedItems) {
                try {
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            // 1. Convertir la entidad a su DTO de subida.
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            Log.d(TAG, "[CATEGORIA] Petición POST a /api/categorias. Body: ${gson.toJson(uploadRequest)}")

                            // 2. Realizar la petición POST.
                            val response = apiRequest<Categoria>(
                                method = HttpMethod.POST,
                                url = "/api/categorias",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Categoria>() {}.type
                            )

                            // 3. Actualizar la entidad local con el ID del servidor.
                            dao.updateServerIdAndStatus(entity.id, response.id ?: 0)
                            Log.i(TAG, "[CATEGORIA] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            // 1. Convertir la entidad a su DTO de subida.
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            Log.d(TAG, "[CATEGORIA] Petición PUT a /api/categorias/${entity.serverId}. Body: ${gson.toJson(uploadRequest)}")

                            // 2. Realizar la petición PUT.
                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/categorias/${entity.serverId}",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type
                            )

                            // 3. Marcar la entidad local como sincronizada.
                            dao.updateStatusToSyncedByServerId(entity.serverId!!)
                            Log.i(TAG, "[CATEGORIA] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[CATEGORIA] Falló subida para LocalID ${entity.id} (ServerID: ${entity.serverId}). Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadCategorias: ${e.message}", e)
        }
    }
    private suspend fun uploadTipoDocumento(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.tipoDocumentoDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) return
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Tipos de Documento] ---")
            for (entity in unsyncedItems) {
                try {
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            val response = apiRequest<TipoDocumento>(
                                method = HttpMethod.POST,
                                url = "/api/tipos_documento",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<TipoDocumento>() {}.type
                            )
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[TIPO_DOCUMENTO] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/tipos_documento/${entity.serverId}",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type
                            )
                            dao.updateStatusToSyncedByServerId(entity.serverId!!)
                            Log.i(TAG, "[TIPO_DOCUMENTO] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[TIPO_DOCUMENTO] Falló subida para LocalID ${entity.id} (ServerID: ${entity.serverId}). Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadTipoDocumento: ${e.message}", e)
        }
    }

    private suspend fun uploadUnidades(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.unidadDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) return
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Unidades] ---")
            for (entity in unsyncedItems) {
                try {
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            val response = apiRequest<Unidad>(
                                method = HttpMethod.POST,
                                url = "/api/unidades",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unidad>() {}.type
                            )
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[UNIDAD] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/unidades/${entity.serverId}",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type
                            )
                            dao.updateStatusToSyncedByServerId(entity.serverId!!)
                            Log.i(TAG, "[UNIDAD] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[UNIDAD] Falló subida para LocalID ${entity.id} (ServerID: ${entity.serverId}). Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadUnidades: ${e.message}", e)
        }
    }
    private suspend fun uploadMonedas(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.monedaDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) return
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Monedas] ---")
            for (entity in unsyncedItems) {
                try {
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            val response = apiRequest<Moneda>(
                                method = HttpMethod.POST,
                                url = "/api/monedas",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Moneda>() {}.type
                            )
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[MONEDA] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/monedas/${entity.serverId}",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type
                            )
                            dao.updateStatusToSyncedByServerId(entity.serverId!!)
                            Log.i(TAG, "[MONEDA] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[MONEDA] Falló subida para LocalID ${entity.id} (ServerID: ${entity.serverId}). Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadMonedas: ${e.message}", e)
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
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val uploadRequest = db.clienteDao().getConDetallesById(entity.id)?.toDomainModel()?.toUploadRequest() ?: continue
                            val response = apiRequest<Cliente>(
                                method = HttpMethod.POST,
                                url = "/api/clientes",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Cliente>() {}.type
                            )
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[CLIENTE] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val uploadRequest = db.clienteDao().getConDetallesById(entity.id)?.toDomainModel()?.toUploadRequest() ?: continue
                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/clientes/${entity.serverId}",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type
                            )
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


    private suspend fun uploadProductos(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.productoDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) return
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Productos] ---")
            for (entity in unsyncedItems) {
                try {
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val uploadRequest = db.productoDao().getConDetallesById(entity.id)?.toDomainModel()?.toUploadRequest() ?: continue
                            val response = apiRequest<Producto>(
                                method = HttpMethod.POST,
                                url = "/api/productos",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Producto>() {}.type
                            )
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[PRODUCTO] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val uploadRequest = db.productoDao().getConDetallesById(entity.id)?.toDomainModel()?.toUploadRequest() ?: continue
                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/productos/${entity.serverId}",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type
                            )
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
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            val response = apiRequest<Familia>(
                                method = HttpMethod.POST,
                                url = "/api/familias",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Familia>() {}.type
                            )
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[FAMILIA] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/familias/${entity.serverId}",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type
                            )
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
        val formaPagoDao = db.formaPagoDao()
        val tipoFormaPagoDao = db.tipoFormaPagoDao()

        try {
            val unsyncedItems = formaPagoDao.getUnsynced()
            if (unsyncedItems.isEmpty()) {
                return
            }
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Formas de Pago] ---")

            for (entity in unsyncedItems) {
                try {
                    // 1. Convertir la entidad base a su modelo de dominio.
                    val domainModel = entity.toDomainModel()

                    // 2. CORRECCIÓN: Buscar la entidad relacionada (TipoFormaPago) usando su serverId,
                    // que está guardado en el campo 'tipoFormaPagoId' de la FormaPago.
                    if (entity.tipoFormaPagoId != null) {
                        val tipoFormaPagoEntity = tipoFormaPagoDao.findByServerId(entity.tipoFormaPagoId)
                        if (tipoFormaPagoEntity != null) {
                            domainModel.tipoFormaPago = tipoFormaPagoEntity.toDomainModel()
                        } else {
                            Log.w(TAG, "[FORMA_PAGO] No se encontró el TipoFormaPago con ServerID ${entity.tipoFormaPagoId} para la Forma de Pago LocalID ${entity.id}. Se subirá sin esta relación.")
                        }
                    }

                    // 3. Proceder con la lógica de subida usando el DTO correcto.
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val uploadRequest = domainModel.toUploadRequest()
                            Log.d(TAG, "[FORMA_PAGO] Petición POST a /api/formas_pagos. Body: ${gson.toJson(uploadRequest)}")

                            val response = apiRequest<FormaPago>(
                                method = HttpMethod.POST,
                                url = "/api/formas_pagos",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<FormaPago>() {}.type
                            )

                            formaPagoDao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[FORMA_PAGO] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val uploadRequest = domainModel.toUploadRequest()
                            Log.d(TAG, "[FORMA_PAGO] Petición PUT a /api/formas_pagos/${entity.serverId}. Body: ${gson.toJson(uploadRequest)}")

                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/formas_pagos/${entity.serverId}",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type
                            )

                            formaPagoDao.updateStatusToSyncedByServerId(entity.serverId!!)
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
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            val response = apiRequest<Promocion>(
                                method = HttpMethod.POST,
                                url = "/api/promociones",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Promocion>() {}.type
                            )
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[PROMOCION] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/promociones/${entity.serverId}",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type
                            )
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
        val renglonDao = db.renglonComprobanteDao() // DAO para acceder a los renglones locales
        val pagoDao = db.comprobantePagoDao()
        val promocionDao = db.comprobantePromocionDao()
        val formaPagoDao = db.formaPagoDao()
        val promocionDaoForDetails = db.promocionDao()
        val promocionJoinDao = db.comprobantePromocionDao()
        val promocionDetailDao = db.promocionDao()
        try {
            val unsyncedItems = comprobanteDao.getUnsynced()
            if (unsyncedItems.isEmpty()) return
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Comprobantes] ---")

            for (entity in unsyncedItems) {
                try {
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            val comprobanteDomain = entity.toDomainModel()

                            // --- INICIO DE LA MODIFICACIÓN: Cargar y adjuntar detalles ---
                            // 2. Cargar y mapear los pagos asociados
                            val pagoEntities = pagoDao.getByComprobanteLocalId(entity.id.toLong())
                            val formasDePagoComprobante = pagoEntities.mapNotNull { pagoEntity ->
                                val formaPagoEntity = formaPagoDao.findByServerId(pagoEntity.formaPagoId)
                                formaPagoEntity?.let {
                                    FormaPagoComprobante(
                                        id = it.serverId ?: 0,
                                        nombre = it.nombre ?: "",
                                        porcentaje = it.porcentaje,
                                        importe = String.format(Locale.US, "%.2f", pagoEntity.importe),
                                        tipoFormaPago = TipoFormaPago() // Placeholder, no es crítico para la subida
                                    )
                                }
                            }
                            comprobanteDomain.formas_de_pago = formasDePagoComprobante

                            val promocionRelations = promocionJoinDao.getByComprobanteLocalId(entity.id.toLong())

                            // 4. Buscar los detalles de cada promoción y crear la lista final
                            val promocionesComprobante = promocionRelations.mapNotNull { relation ->
                                // Usamos el ID de la promoción para buscar sus detalles completos
                                promocionDetailDao.findByServerId(relation.promocionId)?.toDomainModel()
                            }
                            comprobanteDomain.promociones = promocionesComprobante


                                val comprobanteUploadRequest = comprobanteDomain.toUploadRequest()

                                Log.d(TAG, "[COMPROBANTE] Petición POST. Body: ${gson.toJson(comprobanteUploadRequest)}")
                                val responseComprobante = apiRequest<Comprobante>(
                                    method = HttpMethod.POST,
                                    url = "/api/comprobantes/",
                                    headers = headers,
                                    body = comprobanteUploadRequest,
                                    responseType = object : TypeToken<Comprobante>() {}.type
                                )
                            val newServerId = responseComprobante.id
                            Log.i(TAG, "[COMPROBANTE] Éxito POST. LocalID ${entity.id} -> ServerID $newServerId.")

                            // --- INICIO: LÓGICA PARA SUBIR RENGLONES ---
                            var allRenglonesUploaded = true
                            val renglonEntities = renglonDao.getByComprobante(entity.id).first()

                            if (renglonEntities.isNotEmpty()) {
                                Log.d(TAG, "[RENGLONES] Subiendo ${renglonEntities.size} renglones para Comprobante ServerID $newServerId...")
                                for (renglonEntity in renglonEntities) {
                                    try {
                                        val renglonModel = gson.fromJson(renglonEntity.data, RenglonComprobante::class.java)
                                        val renglonUploadRequest = renglonModel.toUploadRequest(comprobanteServerId = newServerId)

                                        // Petición POST para cada renglón
                                        apiRequest<Unit>(
                                            method = HttpMethod.POST,
                                            url = "/api/renglones_comprobantes/$newServerId/",
                                            headers = headers,
                                            body = renglonUploadRequest,
                                            responseType = object : TypeToken<Unit>() {}.type
                                        )
                                    } catch (renglonError: Exception) {
                                        allRenglonesUploaded = false
                                        Log.e(TAG, "[RENGLON] Falló subida para Comprobante ServerID $newServerId. Renglón LocalID: ${renglonEntity.id}. Error: ${renglonError.message}")
                                    }
                                }
                            }
                            // --- FIN: LÓGICA PARA SUBIR RENGLONES ---

                            // 3. Marcar como sincronizado solo si el comprobante Y TODOS sus renglones se subieron
                            if (allRenglonesUploaded) {
                                comprobanteDao.updateServerIdAndStatus(entity.id, newServerId)
                                Log.i(TAG, "[COMPROBANTE] Sincronización completa para LocalID ${entity.id}.")
                            } else {
                                Log.w(TAG, "[COMPROBANTE] Sincronización incompleta para LocalID ${entity.id}. Se reintentará más tarde.")
                            }
                        }
                        SyncStatus.UPDATED -> {
                            // La lógica para actualizar comprobantes existentes se mantiene
                            if (entity.serverId == null) {
                                Log.e(TAG, "[COMPROBANTE] Error: ServerID es nulo para un comprobante marcado como UPDATED. LocalID: ${entity.id}")
                                continue
                            }
                            val comprobanteUploadRequest = entity.toDomainModel().toUploadRequest()
                            Log.d(TAG, "[COMPROBANTE] Petición PUT a /api/comprobantes/${entity.serverId}. Body: ${gson.toJson(comprobanteUploadRequest)}")

                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/comprobantes/${entity.serverId}/",
                                headers = headers,
                                body = comprobanteUploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type
                            )
                            comprobanteDao.updateServerIdAndStatus(entity.serverId!!,entity.serverId!!)
                            Log.i(TAG, "[COMPROBANTE] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[COMPROBANTE] Falló subida para LocalID ${entity.id}. Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadComprobantes: ${e.message}", e)
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
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            val response = apiRequest<Proveedor>(
                                method = HttpMethod.POST,
                                url = "/api/proveedores",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Proveedor>() {}.type
                            )
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[PROVEEDOR] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/proveedores/${entity.serverId}",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type
                            )
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
    private suspend fun uploadTipoIVA(db: AppDatabase, headers: MutableMap<String?, String?>) {
        val dao = db.tipoIvaDao()
        try {
            val unsyncedItems = dao.getUnsynced()
            if (unsyncedItems.isEmpty()) {
                return
            }
            Log.d(TAG, "--- Subiendo ${unsyncedItems.size} cambios de [Tipos de IVA] ---")

            for (entity in unsyncedItems) {
                try {
                    when (entity.syncStatus) {
                        SyncStatus.CREATED -> {
                            // 1. Convertir la entidad a su DTO de subida.
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            Log.d(TAG, "[TIPO_IVA] Petición POST a /api/tipos_iva. Body: ${gson.toJson(uploadRequest)}")

                            // 2. Realizar la petición POST.
                            val response = apiRequest<TipoIVA>(
                                method = HttpMethod.POST,
                                url = "/api/tipos_iva",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<TipoIVA>() {}.type
                            )

                            // 3. Actualizar la entidad local con el ID del servidor.
                            dao.updateServerIdAndStatus(entity.id, response.id)
                            Log.i(TAG, "[TIPO_IVA] Éxito POST: LocalID=${entity.id} ahora es ServerID=${response.id}")
                        }
                        SyncStatus.UPDATED -> {
                            // 1. Convertir la entidad a su DTO de subida.
                            val uploadRequest = entity.toDomainModel().toUploadRequest()
                            Log.d(TAG, "[TIPO_IVA] Petición PUT a /api/tipos_iva/${entity.serverId}. Body: ${gson.toJson(uploadRequest)}")

                            // 2. Realizar la petición PUT.
                            apiRequest<Unit>(
                                method = HttpMethod.PUT,
                                url = "/api/tipos_iva/${entity.serverId}",
                                headers = headers,
                                body = uploadRequest,
                                responseType = object : TypeToken<Unit>() {}.type
                            )

                            // 3. Marcar la entidad local como sincronizada.
                            dao.updateStatusToSyncedByServerId(entity.serverId!!)
                            Log.i(TAG, "[TIPO_IVA] Éxito PUT: ServerID=${entity.serverId} actualizado.")
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[TIPO_IVA] Falló subida para LocalID ${entity.id} (ServerID: ${entity.serverId}). Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en uploadTiposDeIVA: ${e.message}", e)
        }
    }
    private suspend fun <T> apiRequest(
        method: HttpMethod,
        url: String,
        headers: MutableMap<String?, String?>,
        body: Any? = null,
        responseType: Type = object : TypeToken<T>() {}.type
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