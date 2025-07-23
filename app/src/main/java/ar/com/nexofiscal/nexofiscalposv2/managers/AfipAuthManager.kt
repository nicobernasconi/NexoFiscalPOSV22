package ar.com.nexofiscal.nexofiscalposv2.managers

import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.models.AfipAuthRequest
import ar.com.nexofiscal.nexofiscalposv2.models.AfipAuthResponse
import ar.com.nexofiscal.nexofiscalposv2.network.AfipApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Gestiona la obtención del token y sign de autenticación desde el AFIP SDK.
 */
object AfipAuthManager {

    private const val TAG = "AfipAuthManager"

    /**
     * Obtiene el token y sign de AFIP.
     * @param cuit El CUIT de la empresa.
     * @param cert El certificado en formato string.
     * @param key La clave privada en formato string.
     * @return Un objeto [AfipAuthResponse] si la petición es exitosa, o null si falla.
     */
    suspend fun getAuthToken(
        cuit: String,
        cert: String,
        key: String
    ): AfipAuthResponse? {
        val request = AfipAuthRequest(
            taxId = cuit,
            cert = cert,
            key = key
        )

        Log.d(TAG, "Solicitando token de acceso a AFIP SDK...")

        return withContext(Dispatchers.IO) {
            try {
                // Se ejecuta la llamada a la API de forma síncrona dentro de una corrutina de IO
                val response = AfipApiClient.instance.getAccessToken(request).execute()
                if (response.isSuccessful) {
                    Log.i(TAG, "Token de acceso obtenido correctamente.")
                    response.body()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error al obtener el token. Código: ${response.code()}, Mensaje: $errorBody")
                    null
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Excepción HTTP al obtener el token.", e)
                null
            } catch (e: Throwable) {
                Log.e(TAG, "Error desconocido al obtener el token.", e)
                null
            }
        }
    }
}