package ar.com.nexofiscal.nexofiscalposv2.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente de Retrofit configurado para comunicarse con la API de AFIP SDK.
 */
object AfipApiClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://app.afipsdk.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val instance: AfipApiService by lazy {
        retrofit.create(AfipApiService::class.java)
    }
}