package ar.com.nexofiscal.nexofiscalposv2.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET
    fun requestGet(
        @Url url: String?,
        @HeaderMap headers: MutableMap<String?, String?>?
    ): Call<ResponseBody?>?

    @POST
    fun requestPost(
        @Url url: String?,
        @HeaderMap headers: MutableMap<String?, String?>?,
        @Body body: RequestBody?
    ): Call<ResponseBody?>?

    @PUT
    fun requestPut(
        @Url url: String?,
        @HeaderMap headers: MutableMap<String?, String?>?,
        @Body body: RequestBody?
    ): Call<ResponseBody?>?

    @DELETE
    fun requestDelete(
        @Url url: String?,
        @HeaderMap headers: MutableMap<String?, String?>?
    ): Call<ResponseBody?>?
}
