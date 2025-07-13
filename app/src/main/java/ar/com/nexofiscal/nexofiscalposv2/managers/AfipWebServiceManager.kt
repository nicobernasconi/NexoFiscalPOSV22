package ar.com.nexofiscal.nexofiscalposv2.managers

import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.models.*
import ar.com.nexofiscal.nexofiscalposv2.network.ApiCallback
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.network.HttpMethod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Headers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object AfipWebServiceManager {

    private const val TAG = "AfipWebServiceManager"
    // Asumimos que el endpoint en tu servidor es /api/afip
    private const val AFIP_ENDPOINT = "/api/afip"

    suspend fun crearComprobanteElectronico(
        // NOTA: El token y sign deben ser obtenidos de una llamada previa a tu backend
        token: String,
        sign: String,
        cuit: String,
        comprobante: Comprobante,
        renglones: List<RenglonComprobante>
    ): Comprobante {

        // --- 1. Mapear datos del comprobante de la app a los de AFIP ---
        val tipoCbteAfip = mapTipoComprobante(comprobante.tipoComprobante?.id)
        val fechaAfip = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        // --- 2. Calcular importes y mapear renglones ---
        val subtotalesIva = renglones
            .filter { it.tasaIva > 0 }
            .groupBy { it.tasaIva }
            .map { (tasa, items) ->
                val baseImponible = items.sumOf { it.totalLinea.toDouble() / (1 + tasa) }
                val importeIva = items.sumOf { baseImponible * tasa }
                AlicIva(
                    id = mapTasaIva(tasa),
                    baseImp = "%.2f".format(baseImponible).toDouble(),
                    importe = "%.2f".format(importeIva).toDouble()
                )
            }

        val impNeto = subtotalesIva.sumOf { it.baseImp }
        val impIVA = subtotalesIva.sumOf { it.importe }
        val impTotal = comprobante.total!!.toDouble()

        // --- 3. Construir el objeto de la petición ---
        val auth = Auth(token, sign, cuit)
        val feCabReq = FeCabReq(ptoVta = comprobante.puntoVenta!!, cbteTipo = tipoCbteAfip)

        val feDetRequest = FECAEDetRequest(
            docTipo = comprobante.cliente?.tipoDocumento?.id ?: 99,
            docNro = comprobante.cliente?.numeroDocumento?.toLongOrNull() ?: 0,
            cbteDesde = (comprobante.numeroFactura ?: 1).toLong(), // Se debe obtener el próximo número
            cbteHasta = (comprobante.numeroFactura ?: 1).toLong(),
            cbteFch = fechaAfip,
            impTotal = impTotal,
            impNeto = impNeto,
            impIVA = impIVA,
            iva = if (subtotalesIva.isNotEmpty()) IvaData(subtotalesIva) else null
            // Aquí iría la lógica para CbtesAsoc en notas de crédito
        )

        val feCAEReq = FeCAEReq(feCabReq, FeDetReq(feDetRequest))
        val request = AfipWsRequest(params = Params(auth, feCAEReq))

        Log.d(TAG, "Enviando a NexoFiscal: ${Gson().toJson(request)}")

        // --- 4. Realizar la llamada a la API ---
        return suspendCoroutine { continuation ->
            ApiClient.request(
                method = HttpMethod.POST,
                url = AFIP_ENDPOINT,
                headers = null, // El token va en el cuerpo según la estructura
                bodyObject = request,
                responseType = object : TypeToken<Comprobante>() {}.type, // Asume que la respuesta es un Comprobante
                callback = object : ApiCallback<Comprobante?> {
                    override fun onSuccess(statusCode: Int, headers: Headers?, payload: Comprobante?) {
                        if (payload != null) {
                            Log.i(TAG, "Factura creada exitosamente en AFIP. CAE: ${payload.cae}")
                            continuation.resume(payload)
                        } else {
                            continuation.resumeWithException(Exception("Respuesta de la API vacía."))
                        }
                    }

                    override fun onError(statusCode: Int, errorMessage: String?) {
                        continuation.resumeWithException(Exception("Error $statusCode: $errorMessage"))
                    }
                }
            )
        }
    }

    private fun mapTipoComprobante(tipoId: Int?): Int {
        return when (tipoId) {
            1 -> 1   // Factura A
            2 -> 6   // Factura B (en la app es Presupuesto) -> Mapeado a Factura B
            3 -> 11  // Factura C (en la app es Pedido) -> Mapeado a Factura C
            4 -> 3   // Nota de Crédito A
            5 -> 8   // Nota de Crédito B
            6 -> 13  // Nota de Crédito C
            else -> throw IllegalArgumentException("Tipo de comprobante no mapeado a AFIP: $tipoId")
        }
    }

    private fun mapTasaIva(tasa: Double): Int {
        return when (tasa) {
            0.21 -> 5
            0.105 -> 4
            0.27 -> 6
            0.05 -> 8
            0.025 -> 9
            0.0 -> 3
            else -> throw IllegalArgumentException("Tasa de IVA no mapeada a AFIP: $tasa")
        }
    }
}