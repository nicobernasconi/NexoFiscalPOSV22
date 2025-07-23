package ar.com.nexofiscal.nexofiscalposv2.network

import ar.com.nexofiscal.nexofiscalposv2.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AfipApiService {
    @POST("api/v1/afip/auth")
    fun getAccessToken(@Body body: AfipAuthRequest): Call<AfipAuthResponse>

    // --- INICIO DE LA MODIFICACIÓN ---
    // Cambiamos el tipo de retorno a nuestro nuevo modelo 'LastVoucherResponse'
    @POST("api/v1/afip/requests")
    fun getLastVoucher(@Body body: LastVoucherRequest): Call<LastVoucherResponse>
    // --- FIN DE LA MODIFICACIÓN ---

    @POST("api/v1/afip/requests")
    fun createVoucher(@Body body: CreateVoucherRequest): Call<CreateVoucherResponse>
}