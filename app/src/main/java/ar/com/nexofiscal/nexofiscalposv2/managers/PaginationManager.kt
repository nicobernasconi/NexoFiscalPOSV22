package ar.com.nexofiscal.nexofiscalposv2.managers

import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Headers
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object PaginationManager {

    // --- CAMBIO: El PAGE_SIZE del cliente ahora se usa solo para la petición inicial ---
    private const val PAGE_SIZE = 255

    suspend fun <T> fetchAllPages(
        endpoint: String,
        headers: MutableMap<String?, String?>?,
        responseType: java.lang.reflect.Type,
        onProgress: (count: Int) -> Unit = { }
    ): List<T?> {
        val allItems = mutableListOf<T?>()
        var currentPage = 1

        // --- CAMBIO: Nueva lógica de paginación basada en cabeceras ---

        // 1. Pedimos la primera página para obtener las cabeceras y el total de páginas.
        val (firstPageResult, responseHeaders) = fetchPage<T>(endpoint, headers, currentPage, responseType)
        if (firstPageResult.isEmpty()) {
            onProgress(0)
            return emptyList()
        }
        allItems.addAll(firstPageResult)
        onProgress(allItems.size)

        // 2. Leemos la cabecera 'X-Total-Pages' para saber cuántas páginas faltan.
        val totalPages = responseHeaders?.get("X-Total-Pages")?.toIntOrNull() ?: 1

        // 3. Si hay más de una página, iteramos hasta el total que nos indicó el servidor.
        if (totalPages > 1) {
            for (page in 2..totalPages) {
                val (nextPageResult, _) = fetchPage<T>(endpoint, headers, page, responseType)
                if (nextPageResult.isNotEmpty()) {
                    allItems.addAll(nextPageResult)
                    onProgress(allItems.size)
                } else {
                    // Si una página intermedia viene vacía, rompemos por seguridad.
                    break
                }
            }
        }

        return allItems
    }

    // --- CAMBIO: fetchPage ahora devuelve también las cabeceras de la respuesta ---
    private suspend fun <T> fetchPage(
        endpoint: String,
        headers: MutableMap<String?, String?>?,
        page: Int,
        responseType: java.lang.reflect.Type
    ): Pair<List<T?>, Headers?> = suspendCancellableCoroutine { continuation ->
        val url = "$endpoint?page=$page&limit=$PAGE_SIZE"

        ApiClient.request(
            HttpMethod.GET,
            url,
            headers,
            null,
            responseType,
            object : ApiCallback<MutableList<T?>?> {
                override fun onSuccess(statusCode: Int, responseHeaders: Headers?, payload: MutableList<T?>?) {
                    if (continuation.isActive) {
                        continuation.resume(Pair(payload ?: emptyList(), responseHeaders))
                    }
                }

                override fun onError(statusCode: Int, errorMessage: String?) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            Exception("Error en la página $page para $endpoint: $errorMessage (Code: $statusCode)")
                        )
                    }
                }
            }
        )
    }
}