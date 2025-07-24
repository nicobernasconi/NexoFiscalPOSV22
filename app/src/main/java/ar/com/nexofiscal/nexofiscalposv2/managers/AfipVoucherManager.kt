package ar.com.nexofiscal.nexofiscalposv2.managers

import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.models.*
import ar.com.nexofiscal.nexofiscalposv2.network.AfipApiClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gestiona la obtención del último número de comprobante y la creación de facturas electrónicas
 * utilizando la estructura de petición correcta para el AFIP SDK.
 */
object AfipVoucherManager {

    private const val TAG = "AfipVoucherManager"

    /**
     * Obtiene el último número de comprobante utilizado para un tipo y punto de venta específicos.
     * @return El número del último comprobante, o null si falla.
     */
    suspend fun getLastVoucherNumber(
        auth: AfipAuthResponse,
        cuit: String,
        pointOfSale: Int,
        voucherType: Int
    ): Int? {
        // 1. Construir el payload de autenticación que va dentro de 'params'
        val authPayload = AuthPayload(token = auth.token, sign = auth.sign, cuit = cuit)

        // 2. Construir el objeto 'params' con la autenticación y los datos específicos del método
        val params = LastVoucherParams(
            auth = authPayload,
            pointOfSale = pointOfSale,
            voucherType = voucherType
        )

        // 3. Construir la petición final con el método y los parámetros anidados
        val request = LastVoucherRequest(params = params)

        Log.d(TAG, "Solicitando último número. Petición: ${Gson().toJson(request)}")

        return withContext(Dispatchers.IO) {
            try {
                val response = AfipApiClient.instance.getLastVoucher(request).execute()
                if (response.isSuccessful) {
                    // Acceder a la propiedad anidada 'result' y luego a 'voucherNumber'
                    val lastVoucher = response.body()?.result?.voucherNumber
                    Log.i(TAG, "Último número de comprobante obtenido: $lastVoucher")
                    lastVoucher
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error al obtener último número. Código: ${response.code()}, Mensaje: $errorBody")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al obtener último número.", e)
                null
            }
        }
    }

    /**
     * Crea una factura electrónica y solicita el CAE.
     * @param comprobante El objeto de la app con todos los datos de la venta.
     * @return Un objeto [CreateVoucherDetailResponse] con el CAE y Vencimiento si es exitoso.
     * @throws Exception si AFIP rechaza la solicitud o si ocurre un error de comunicación.
     */
    suspend fun createElectronicVoucher(
        auth: AfipAuthResponse,
        cuit: String,
        comprobante: Comprobante
    ): CreateVoucherDetailResponse {
        val nextVoucherNumber = (comprobante.numeroFactura ?: 0).toLong()
        val authPayload = AuthPayload(token = auth.token, sign = auth.sign, cuit = cuit)

        val feCabReq = FeCabReq(ptoVta = comprobante.puntoVenta!!, cbteTipo = comprobante.tipoFactura!!)
        val fechaAfip = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        val subtotalesIva = listOf(
            if (comprobante.importeIva21!! > 0) AlicIva(id = 5, baseImp = comprobante.noGravadoIva21!!, importe = comprobante.importeIva21!!) else null,
            if (comprobante.importeIva105!! > 0) AlicIva(id = 4, baseImp = comprobante.noGravadoIva105!!, importe = comprobante.importeIva105!!) else null,
            if (comprobante.noGravadoIva0!! > 0) AlicIva(id = 3, baseImp = comprobante.noGravadoIva0!!, importe = 0.0) else null
        ).filterNotNull()

        // Determinar CondicionIVAReceptorId basado en el tipo de factura y la condición de IVA del cliente
        // Referencia: https://www.afip.gob.ar/ws/WSCI/tabla_CondicionIVAReceptorId.txt
        // 1=IVA Responsable Inscripto, 2=IVA Responsable No Inscripto, 3=IVA No Responsable,
        // 4=IVA Sujeto Exento, 5=Consumidor Final, 6=Responsable Monotributo, 7=Sujeto No Categorizado,
        // 8=Proveedor del Exterior, 9=Cliente del Exterior, 10=IVA Liberado Ley Nº 19.640,
        // 11=IVA Responsable Inscripto Agente de Percepción, 12=Pequeño Contribuyente Eventual,
        // 13=Monotributista Social, 14=Pequeño Contribuyente Eventual Social
        val clienteSeleccionado = comprobante.cliente
        val condicionIvaCliente = clienteSeleccionado?.tipoIva?.nombre
            ?.uppercase(Locale.getDefault()) ?: "CONSUMIDOR" // Predeterminado a Consumidor Final
        val condicionIvaReceptorId = when {
            condicionIvaCliente.contains("INSCRIPTO") -> 1
            condicionIvaCliente.contains("NO INSCRIPTO") -> 2 // Generalmente significa Monotributista o Exento en la práctica para la app
            condicionIvaCliente.contains("NO RESPONSABLE") -> 3
            condicionIvaCliente.contains("EXENTO") -> 4
            condicionIvaCliente.contains("CONSUMIDOR") -> 5
            condicionIvaCliente.contains("MONOTRIBUTO") -> 6
            condicionIvaCliente.contains("NO CATEGORIZADO") -> 7
            condicionIvaCliente.contains("PROVEEDOR DEL EXTERIOR") -> 8
            condicionIvaCliente.contains("CLIENTE DEL EXTERIOR") -> 9
            condicionIvaCliente.contains("LIBERADO") -> 10
            else -> throw Exception("Condición de IVA del cliente no válida: $condicionIvaCliente")
        }



        val feDetRequest = FECAEDetRequest(
            docTipo = comprobante.tipoDocumento!!,
            docNro = comprobante.numeroDeDocumento!!,
            cbteDesde = nextVoucherNumber,
            cbteHasta = nextVoucherNumber,
            cbteFch = fechaAfip,
            impTotal = comprobante.total!!.toDouble(),
            impNeto = comprobante.noGravado!!,
            impIVA = comprobante.importeIva!!,
            iva = if (subtotalesIva.isNotEmpty()) IvaData(subtotalesIva) else null,
            condicionIVAReceptorId = condicionIvaReceptorId,
        )

        val feCAEReq = FeCAEReq(feCabReq, FeDetReq(feDetRequest))
        val params = CreateVoucherParams(auth = authPayload, feCAEReq = feCAEReq)
        val request = CreateVoucherRequest(params = params)

        Log.d(TAG, "Creando factura N°$nextVoucherNumber. Petición: ${Gson().toJson(request)}")

        return withContext(Dispatchers.IO) {
            try {
                val response = AfipApiClient.instance.createVoucher(request).execute()
                if (response.isSuccessful) {
                    val afipResult = response.body()?.result

                    // Validar si la respuesta de AFIP es un rechazo
                    if (afipResult?.feCabResp?.resultado == "R") {
                        val errorMsg = afipResult.errors?.errorDetails?.firstOrNull()?.message
                            ?: "AFIP rechazó la solicitud sin un mensaje claro."
                        throw Exception(errorMsg)
                    }

                    // Si es Aprobado, extraer el CAE y la fecha de vencimiento
                    val detailResponse = afipResult?.feDetResp?.FECAEDetResponse?.firstOrNull()
                    if (detailResponse?.resultado == "A" && !detailResponse.cae.isNullOrBlank()) {
                        Log.i(TAG, "CAE obtenido con éxito: ${detailResponse.cae}")
                        detailResponse
                    } else {
                        throw Exception("Respuesta de AFIP Aprobada pero sin CAE válido.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error al crear la factura. Código: ${response.code()}, Mensaje: $errorBody")
                    throw Exception("Error de comunicación con el servidor de AFIP (${response.code()})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al crear la factura: ${e.message}")
                // Re-lanzar la excepción para que sea capturada en la capa superior (UI)
                throw e
            }
        }
    }
}