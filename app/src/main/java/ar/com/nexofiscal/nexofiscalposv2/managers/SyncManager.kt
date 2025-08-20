package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toEntity
import ar.com.nexofiscal.nexofiscalposv2.models.Agrupacion
import ar.com.nexofiscal.nexofiscalposv2.models.Categoria
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.models.Comprobante
import ar.com.nexofiscal.nexofiscalposv2.models.Familia
import ar.com.nexofiscal.nexofiscalposv2.models.FormaPago
import ar.com.nexofiscal.nexofiscalposv2.models.Gasto
import ar.com.nexofiscal.nexofiscalposv2.models.Localidad
import ar.com.nexofiscal.nexofiscalposv2.models.Moneda
import ar.com.nexofiscal.nexofiscalposv2.models.Notificacion
import ar.com.nexofiscal.nexofiscalposv2.models.Pais
import ar.com.nexofiscal.nexofiscalposv2.models.Producto
import ar.com.nexofiscal.nexofiscalposv2.models.Promocion
import ar.com.nexofiscal.nexofiscalposv2.models.Proveedor
import ar.com.nexofiscal.nexofiscalposv2.models.Provincia
import ar.com.nexofiscal.nexofiscalposv2.models.Rol
import ar.com.nexofiscal.nexofiscalposv2.models.StockProducto
import ar.com.nexofiscal.nexofiscalposv2.models.Sucursal
import ar.com.nexofiscal.nexofiscalposv2.models.TasaIva
import ar.com.nexofiscal.nexofiscalposv2.models.Tipo
import ar.com.nexofiscal.nexofiscalposv2.models.TipoComprobante
import ar.com.nexofiscal.nexofiscalposv2.models.TipoDocumento
import ar.com.nexofiscal.nexofiscalposv2.models.TipoFormaPago
import ar.com.nexofiscal.nexofiscalposv2.models.TipoIVA
import ar.com.nexofiscal.nexofiscalposv2.models.Unidad
import ar.com.nexofiscal.nexofiscalposv2.models.Usuario
import ar.com.nexofiscal.nexofiscalposv2.models.Vendedor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object SyncManager {

    private const val TAG = "SyncManager"

    private val _progressState = MutableStateFlow(SyncProgress())
    val progressState = _progressState.asStateFlow()

    private suspend fun <T : Any> executeSyncTask(
        taskName: String,
        endpoint: String,
        typeToken: java.lang.reflect.Type,
        headers: MutableMap<String?, String?>,
        saver: suspend (List<T>) -> Unit
    ) {
        _progressState.update { it.copy(currentTaskName = taskName, currentTaskItemCount = 0) }
        try {
            val allItems = PaginationManager.fetchAllPages<T>(
                endpoint = endpoint,
                headers = headers,
                responseType = typeToken,
                onProgress = { count ->
                    _progressState.update { it.copy(currentTaskItemCount = count) }
                }
            )
            val nonNullItems = allItems.filterNotNull()
            saver(nonNullItems)
            Log.d(TAG, "Tarea '$taskName' completada con ${nonNullItems.size} ítems.")
        } catch (e: Exception) {
            val errorMessage = "Error en $taskName: ${e.message}"
            Log.e(TAG, errorMessage, e)
            _progressState.update { it.copy(errors = it.errors + errorMessage) }
        }
    }

    suspend fun startFullSync(context: Context, token: String) {
        val db = AppDatabase.getInstance(context)
        val headers = mutableMapOf<String?, String?>("Authorization" to "Bearer $token")

        val tasks: List<suspend () -> Unit> = listOf(
            {

                _progressState.update { it.copy(currentTaskName = "Agrupacion", currentTaskItemCount = 0) }
                executeSyncTask<Agrupacion>(
                    "Agrupaciones",
                    "/api/agrupaciones",
                    object : com.google.gson.reflect.TypeToken<List<Agrupacion>>() {}.type,
                    headers
                ) { db.agrupacionDao().upsertAll(it.map(Agrupacion::toEntity)) } // <-- CAMBIO AQUÍ
            },
            {
                _progressState.update { it.copy(currentTaskName = "Notificaciones", currentTaskItemCount = 0) }
                executeSyncTask<Notificacion>(
                    "Notificaciones",
                    "/api/notificaciones",
                    object : com.google.gson.reflect.TypeToken<List<Notificacion>>() {}.type,
                    headers
                ) { db.notificacionDao().upsertAll(it.map(Notificacion::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Tipos", currentTaskItemCount = 0) }
                // Corregimos el tipo genérico de Agrupacion a Tipo
                executeSyncTask<Tipo>(
                    "Tipos",
                    "/api/tipos",
                    object : com.google.gson.reflect.TypeToken<List<Tipo>>() {}.type,
                    headers
                ) { db.tipoDao().upsertAll(it.map(Tipo::toEntity)) }
            },
            {

                _progressState.update { it.copy(currentTaskName = "Tipos", currentTaskItemCount = 0) }

                executeSyncTask<Categoria>(
                    "Categorías",
                    "/api/categorias",
                    object : com.google.gson.reflect.TypeToken<List<Categoria>>() {}.type,
                    headers
                ) { db.categoriaDao().upsertAll(it.map(Categoria::toEntity)) } // <-- CAMBIO AQUÍ
            },
            {
                _progressState.update { it.copy(currentTaskName = "Clientes", currentTaskItemCount = 0) }
                executeSyncTask<Cliente>(
                    "Clientes",
                    "/api/clientes",
                    object : com.google.gson.reflect.TypeToken<List<Cliente>>() {}.type,
                    headers
                ) { db.clienteDao().upsertAll(it.map(Cliente::toEntity)) } // <-- Usamos la nueva función upsertAll
            },
            {
                _progressState.update { it.copy(currentTaskName = "Formas de Pago", currentTaskItemCount = 0) }
                executeSyncTask<FormaPago>(
                    "Formas de Pago",
                    "/api/formas_pagos",
                    object : com.google.gson.reflect.TypeToken<List<FormaPago>>() {}.type,
                    headers
                ) { db.formaPagoDao().upsertAll(it.map(FormaPago::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Productos", currentTaskItemCount = 0) }

                executeSyncTask<Producto>(
                    "Productos",
                    "/api/productos?activo=1",
                    object : com.google.gson.reflect.TypeToken<List<Producto>>() {}.type,
                    headers
                ) { db.productoDao().upsertAll(it.map(Producto::toEntity)) } // <-- CAMBIO AQUÍ
            },
            {
                _progressState.update { it.copy(currentTaskName = "Proveedores", currentTaskItemCount = 0) }
                executeSyncTask<Proveedor>(
                    "Proveedores",
                    "/api/proveedores",
                    object : com.google.gson.reflect.TypeToken<List<Proveedor>>() {}.type,
                    headers
                ) { db.proveedorDao().upsertAll(it.map(Proveedor::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Promociones", currentTaskItemCount = 0) }
                executeSyncTask<Promocion>(
                    "Promociones",
                    "/api/promociones",
                    object : com.google.gson.reflect.TypeToken<List<Promocion>>() {}.type,
                    headers
                ) { db.promocionDao().upsertAll(it.map(Promocion::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Vendedores", currentTaskItemCount = 0) }
                executeSyncTask<Usuario>(
                    "Usuarios",
                    "/api/usuarios",
                    object : com.google.gson.reflect.TypeToken<List<Usuario>>() {}.type,
                    headers
                ) { db.usuarioDao().upsertAll(it.map(Usuario::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Vendedores", currentTaskItemCount = 0) }
                executeSyncTask<Vendedor>(
                    "Vendedores",
                    "/api/vendedores",
                    object : com.google.gson.reflect.TypeToken<List<Vendedor>>() {}.type,
                    headers
                ) { db.vendedorDao().upsertAll(it.map(Vendedor::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Tipos de Comprobante", currentTaskItemCount = 0) }
                executeSyncTask<TipoComprobante>(
                    "Tipos de Comprobante",
                    "/api/tipos_comprobante",
                    object : com.google.gson.reflect.TypeToken<List<TipoComprobante>>() {}.type,
                    headers
                ) { db.tipoComprobanteDao().upsertAll(it.map(TipoComprobante::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Tipos de Documento", currentTaskItemCount = 0) }
                executeSyncTask<TipoDocumento>(
                    "Tipos de Documento",
                    "/api/tipos_documento",
                    object : com.google.gson.reflect.TypeToken<List<TipoDocumento>>() {}.type,
                    headers
                ) { db.tipoDocumentoDao().upsertAll(it.map(TipoDocumento::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Tipos de Forma de Pago", currentTaskItemCount = 0) }
                executeSyncTask<TipoFormaPago>(
                    "Tipos de Forma de Pago",
                    "/api/tipos_forma_pago",
                    object : com.google.gson.reflect.TypeToken<List<TipoFormaPago>>() {}.type,
                    headers
                ) { db.tipoFormaPagoDao().upsertAll(it.map(TipoFormaPago::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Tipos de IVA", currentTaskItemCount = 0) }
                executeSyncTask<TipoIVA>(
                    "Tipos de IVA",
                    "/api/tipos_iva",
                    object : com.google.gson.reflect.TypeToken<List<TipoIVA>>() {}.type,
                    headers
                ) { db.tipoIvaDao().upsertAll(it.map(TipoIVA::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Unidades", currentTaskItemCount = 0) }
                executeSyncTask<Unidad>(
                    "Unidades",
                    "/api/unidades",
                    object : com.google.gson.reflect.TypeToken<List<Unidad>>() {}.type,
                    headers
                ) { db.unidadDao().upsertAll(it.map(Unidad::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Monedas", currentTaskItemCount = 0) }
                executeSyncTask<Moneda>(
                    "Monedas",
                    "/api/monedas",
                    object : com.google.gson.reflect.TypeToken<List<Moneda>>() {}.type,
                    headers
                ) { db.monedaDao().upsertAll(it.map(Moneda::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Países", currentTaskItemCount = 0) }
                executeSyncTask<Pais>(
                    "Países",
                    "/api/paises",
                    object : com.google.gson.reflect.TypeToken<List<Pais>>() {}.type,
                    headers
                ) { db.paisDao().upsertAll(it.map(Pais::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Provincias", currentTaskItemCount = 0) }
                executeSyncTask<Provincia>(
                    "Provincias",
                    "/api/provincias",
                    object : com.google.gson.reflect.TypeToken<List<Provincia>>() {}.type,
                    headers
                ) { db.provinciaDao().upsertAll(it.map(Provincia::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Localidades", currentTaskItemCount = 0) }
                executeSyncTask<Localidad>(
                    "Localidades",
                    "/api/localidades",
                    object : com.google.gson.reflect.TypeToken<List<Localidad>>() {}.type,
                    headers
                ) { db.localidadDao().upsertAll(it.map(Localidad::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Roles", currentTaskItemCount = 0) }
                executeSyncTask<Rol>(
                    "Roles",
                    "/api/roles",
                    object : com.google.gson.reflect.TypeToken<List<Rol>>() {}.type,
                    headers
                ) { db.rolDao().upsertAll(it.map(Rol::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Sucursales", currentTaskItemCount = 0) }
                executeSyncTask<Sucursal>(
                    "Sucursales",
                    "/api/sucursales",
                    object : com.google.gson.reflect.TypeToken<List<Sucursal>>() {}.type,
                    headers
                ) { db.sucursalDao().upsertAll(it.map(Sucursal::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Familias", currentTaskItemCount = 0) }
                executeSyncTask<Familia>(
                    "Familias",
                    "/api/familias",
                    object : com.google.gson.reflect.TypeToken<List<Familia>>() {}.type,
                    headers
                ) { db.familiaDao().upsertAll(it.map(Familia::toEntity)) }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Tasas de IVA", currentTaskItemCount = 0) }
                executeSyncTask<TasaIva>(
                    "Tasas de IVA",
                    "/api/tasa_iva",
                    object : com.google.gson.reflect.TypeToken<List<TasaIva>>() {}.type,
                    headers
                ) { db.tasaIvaDao().upsertAll(it.map(TasaIva::toEntity)) }
            },{
                // --- INICIO DE LA CORRECCIÓN ---
                val taskName = "Stocks"
                _progressState.update { it.copy(currentTaskName = taskName, currentTaskItemCount = 0) }
                val sucursalId = SessionManager.sucursalId
                if (sucursalId != -1) {
                    executeSyncTask<StockProducto>(
                        taskName,
                        "/api/productos_stocks?sucursal_id=$sucursalId&",
                        object : com.google.gson.reflect.TypeToken<List<StockProducto>>() {}.type,
                        headers
                    ) { db.stockProductoDao().upsertAll(it.map(StockProducto::toEntity)) }
                } else {
                    val errorMsg = "Error en $taskName: ID de sucursal no encontrado."
                    Log.e(TAG, errorMsg)
                    _progressState.update { it.copy(errors = it.errors + errorMsg) }
                }
                // --- FIN DE LA CORRECCIÓN ---
            },
            {

                val taskName = "Comprobantes"
                _progressState.update {
                    it.copy(
                        currentTaskName = taskName,
                        currentTaskItemCount = 0
                    )
                }
                try {
                    suspendCancellableCoroutine<Unit> { continuation ->
                        val prefs = context.getSharedPreferences("nexofiscal", Context.MODE_PRIVATE)
                        val sucursalId = prefs.getInt("sucursal_id", -1)
                        if (sucursalId == -1) {
                            val errorMsg =
                                "Error en $taskName: No se pudo encontrar el ID de la sucursal."
                            Log.e(TAG, errorMsg)
                            _progressState.update { it.copy(errors = it.errors + errorMsg) }
                            if (continuation.isActive) continuation.resume(Unit)
                            return@suspendCancellableCoroutine
                        }

                        ComprobanteManager.obtenerComprobantes(
                            context,
                            headers,
                            sucursalId,
                            object : ComprobanteManager.ComprobanteListCallback {
                                override fun onSuccess(comprobantes: MutableList<Comprobante?>?) {
                                    _progressState.update {
                                        it.copy(
                                            currentTaskItemCount = comprobantes?.size ?: 0
                                        )
                                    }
                                    Log.d(TAG, "Tarea '$taskName' completada.")
                                    if (continuation.isActive) continuation.resume(Unit)
                                }

                                override fun onError(errorMessage: String?) {
                                    val errorMsg = "Error en $taskName: $errorMessage"
                                    Log.e(TAG, errorMsg)
                                    _progressState.update { it.copy(errors = it.errors + errorMsg) }
                                    if (continuation.isActive) continuation.resume(Unit)
                                }
                            })
                    }
                } catch (e: Exception) {
                    val errorMsg = "Error en $taskName: ${e.message}"
                    Log.e(TAG, errorMsg, e)
                    _progressState.update { it.copy(errors = it.errors + errorMsg) }
                }
            },
            {
                _progressState.update { it.copy(currentTaskName = "Gastos", currentTaskItemCount = 0) }
                executeSyncTask<Gasto>(
                    "Gastos",
                    "/api/gastos",
                    object : com.google.gson.reflect.TypeToken<List<Gasto>>() {}.type,
                    headers
                ) { items ->
                    val entities = items.map(Gasto::toEntity)
                    db.gastoDao().upsertAll(entities)
                }
            }
        )

        _progressState.value = SyncProgress(totalTasks = tasks.size, isFinished = false)

        for ((index, taskAction) in tasks.withIndex()) {
            _progressState.update { it.copy(overallTaskIndex = index + 1) }
            taskAction.invoke()
        }

        _progressState.update {
            it.copy(
                currentTaskName = "Sincronización finalizada",
                isFinished = true
            )
        }
    }


}