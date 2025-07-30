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

    /**
     * Crea una Nota de Crédito basada en un comprobante anulado.
     * @param auth Datos de autenticación de AFIP.
     * @param cuit CUIT de la empresa emisora.
     * @param comprobanteAnulado Comprobante original que se anuló.
     * @return Un objeto [CreateVoucherDetailResponse] con el CAE y la fecha de vencimiento.
     */
    suspend fun createNotaDeCredito(
        auth: AfipAuthResponse,
        cuit: String,
        comprobanteAnulado: Comprobante
    ): CreateVoucherDetailResponse {
        val puntoDeVenta = comprobanteAnulado.puntoVenta!!
        val tipoNotaDeCredito = 13 // Nota de Crédito C

        // Obtener el último número de comprobante para la Nota de Crédito
        val lastVoucher = getLastVoucherNumber(auth, cuit, puntoDeVenta, tipoNotaDeCredito)
        val numeroDeNota = (lastVoucher ?: 0) + 1

        val fechaActual = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        val condicionIvaClienteAnulado = comprobanteAnulado.cliente?.tipoIva?.nombre
            ?.uppercase(Locale.getDefault()) ?: "CONSUMIDOR" // Predeterminado a Consumidor Final
        val condicionIvaReceptorId = when {
            condicionIvaClienteAnulado.contains("INSCRIPTO") -> 1
            condicionIvaClienteAnulado.contains("NO INSCRIPTO") -> 2 // Generalmente significa Monotributista o Exento en la práctica para la app
            condicionIvaClienteAnulado.contains("NO RESPONSABLE") -> 3
            condicionIvaClienteAnulado.contains("EXENTO") -> 4
            condicionIvaClienteAnulado.contains("CONSUMIDOR") -> 5
            condicionIvaClienteAnulado.contains("MONOTRIBUTO") -> 6
            condicionIvaClienteAnulado.contains("NO CATEGORIZADO") -> 7
            condicionIvaClienteAnulado.contains("PROVEEDOR DEL EXTERIOR") -> 8
            condicionIvaClienteAnulado.contains("CLIENTE DEL EXTERIOR") -> 9
            condicionIvaClienteAnulado.contains("LIBERADO") -> 10
            // No se manejan "MONOTRIBUTISTA SOCIAL", "IVA NO ALCANZADO", "MONOTRIBUTO TRABAJADOR INDEPENDIENTE PROMOVIDO" explícitamente aquí,
            // se asume que "MONOTRIBUTO" o "NO CATEGORIZADO" cubrirán esos casos o se usa el valor por defecto.
            else -> 1 // Valor por defecto: Responsable Inscripto
        }

        val feCabReq = FeCabReq(ptoVta = puntoDeVenta, cbteTipo = tipoNotaDeCredito)
        val subtotalesIva = listOf(
            if (comprobanteAnulado.importeIva21!! > 0) AlicIva(id = 5, baseImp = comprobanteAnulado.noGravadoIva21!!, importe = comprobanteAnulado.importeIva21!!) else null,
            if (comprobanteAnulado.importeIva105!! > 0) AlicIva(id = 4, baseImp = comprobanteAnulado.noGravadoIva105!!, importe = comprobanteAnulado.importeIva105!!) else null,
            if (comprobanteAnulado.noGravadoIva0!! > 0) AlicIva(id = 3, baseImp = comprobanteAnulado.noGravadoIva0!!, importe = 0.0) else null
        ).filterNotNull()

        val feDetRequest = FECAEDetRequest(
            docTipo = comprobanteAnulado.tipoDocumento!!,
            docNro = comprobanteAnulado.numeroDeDocumento!!,
            cbteDesde = numeroDeNota.toLong(),
            cbteHasta = numeroDeNota.toLong(),
            cbteFch = fechaActual,
            impTotal = comprobanteAnulado.total!!.toDouble(),
            impNeto = comprobanteAnulado.noGravado!!,
            impIVA = comprobanteAnulado.importeIva!!,
            iva = if (subtotalesIva.isNotEmpty()) IvaData(subtotalesIva) else null,
            condicionIVAReceptorId = condicionIvaReceptorId,
            cbtesAsoc = listOf(
                CbteAsoc(
                    tipo = comprobanteAnulado.tipoFactura!!,
                    ptoVta = comprobanteAnulado.puntoVenta!!,
                    nro = comprobanteAnulado.numeroFactura!!.toLong()
                )
            )
        )

        val feCAEReq = FeCAEReq(feCabReq, FeDetReq(feDetRequest))
        val params = CreateVoucherParams(auth = AuthPayload(auth.token, auth.sign, cuit), feCAEReq = feCAEReq)
        val request = CreateVoucherRequest(params = params)

        Log.d(TAG, "Creando Nota de Crédito N°$numeroDeNota. Petición: ${Gson().toJson(request)}")

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
                    Log.e(TAG, "Error al crear la Nota de Crédito. Código: ${response.code()}, Mensaje: $errorBody")
                    throw Exception("Error de comunicación con el servidor de AFIP (${response.code()})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al crear la Nota de Crédito: ${e.message}")
                throw e
            }
        }
    }

    /**
     * Genera un comprobante de nota de crédito basado en el comprobante que se anuló.
     * @param comprobanteAnulado El comprobante original que se anuló.
     * @return Un nuevo objeto [Comprobante] configurado como nota de crédito.
     */
    fun generarNotaDeCredito(comprobanteAnulado: Comprobante): Comprobante {
        // Determinar el tipo de comprobante para la nota de crédito basado en el tipo de factura original
        // Ref: https://www.afip.gob.ar/fe/documentos/TABLA-Tipos-de-Comprobantes.pdf
        // Si es Factura A -> Nota de Crédito A (código 3)
        // Si es Factura B -> Nota de Crédito B (código 8)
        // Si es Factura C -> Nota de Crédito C (código 13)
        // Si es Factura M -> Nota de Crédito M (código 53)
        val tipoNotaDeCredito = when (comprobanteAnulado.tipoFactura) {
            1 -> 3 // Factura A -> Nota de Crédito A
            6 -> 8 // Factura B -> Nota de Crédito B
            11 -> 13 // Factura C -> Nota de Crédito C
            51 -> 53 // Factura M -> Nota de Crédito M
            else -> throw IllegalArgumentException("Tipo de factura no soportado para generar nota de crédito: ${comprobanteAnulado.tipoFactura}")
        }


        return Comprobante(
            id = 0, // Nuevo comprobante, sin ID asignado aún
            serverId = null,
            cliente = comprobanteAnulado.cliente,
            clienteId = comprobanteAnulado.clienteId,
            tipoComprobante = comprobanteAnulado.tipoComprobante,
            tipoComprobanteId = tipoNotaDeCredito,
            fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
            total = (comprobanteAnulado.total!!.toDouble()).toString(), // Total negativo para la nota de crédito
            totalPagado = comprobanteAnulado.totalPagado,
            descuentoTotal = "0.0", // Sin descuentos
            incrementoTotal = "0.0", // Sin incrementos
            importeIva = (comprobanteAnulado.importeIva!!).toDouble(),
            noGravado = (comprobanteAnulado.noGravado!!).toDouble(),
            importeIva21 = (comprobanteAnulado.importeIva21!!).toDouble(),
            importeIva105 = (comprobanteAnulado.importeIva105!!).toDouble(),
            importeIva0 = (comprobanteAnulado.importeIva0!!).toDouble(),
            noGravadoIva21 = (comprobanteAnulado.noGravadoIva21!!).toDouble(),
            noGravadoIva105 = (comprobanteAnulado.noGravadoIva105!!).toDouble(),
            noGravadoIva0 = (comprobanteAnulado.noGravadoIva0!!).toDouble(),
            numero = null, // Número aún no asignado
            puntoVenta = comprobanteAnulado.puntoVenta,
            empresaId = comprobanteAnulado.empresaId,
            sucursalId = comprobanteAnulado.sucursalId,
            vendedorId = comprobanteAnulado.vendedorId,
            formas_de_pago = emptyList(), // Sin formas de pago
            promociones = emptyList(), // Sin promociones
            cuotas = null,
            remito = null,
            persona = null,
            provinciaId = comprobanteAnulado.provinciaId,
            fechaBaja = null,
            motivoBaja = null,
            fechaProceso = null,
            letra = comprobanteAnulado.letra, // Mantener la misma letra
            numeroFactura = null, // Número aún no asignado
            prefijoFactura = null,
            operacionNegocioId = null,
            retencionIva = null,
            retencionIibb = null,
            retencionGanancias = null,
            porcentajeGanancias = null,
            porcentajeIibb = null,
            porcentajeIva = null,
            condicionVentaId = comprobanteAnulado.condicionVentaId,
            descripcionFlete = null,
            recibo = null,
            observaciones1 = "Nota de crédito por anulación del comprobante ${comprobanteAnulado.numeroFactura}",
            observaciones2 = null,
            observaciones3 = null,
            observaciones4 = null,
            descuento = null,
            descuento1 = null,
            descuento2 = null,
            descuento3 = null,
            descuento4 = null,
            iva2 = null,
            impresa = false,
            cancelado = false,
            nombreCliente = comprobanteAnulado.nombreCliente,
            direccionCliente = comprobanteAnulado.direccionCliente,
            localidadCliente = comprobanteAnulado.localidadCliente,
            garantia = null,
            concepto = null,
            notas = null,
            lineaPagoUltima = null,
            relacionTk = null,
            totalIibb = null,
            importeIibb = null,
            provinciaCategoriaIibbId = null,
            importeRetenciones = null,
            provinciaIvaProveedorId = null,
            gananciasProveedorId = null,
            importeGanancias = null,
            numeroIibb = null,
            numeroGanancias = null,
            gananciasProveedor = null,
            cae = null,
            fechaVencimiento = null,
            remitoCliente = null,
            textoDolares = null,
            comprobanteFinal = null,
            numeroGuia1 = null,
            numeroGuia2 = null,
            numeroGuia3 = null,
            tipoAlicuota1 = null,
            tipoAlicuota2 = null,
            tipoAlicuota3 = null,
            direccionEntrega = null,
            fechaEntrega = null,
            horaEntrega = null,
            tipoFactura = tipoNotaDeCredito, // Usar el tipo de nota de crédito
            tipoDocumento = comprobanteAnulado.tipoDocumento,
            numeroDeDocumento = comprobanteAnulado.numeroDeDocumento,
            qr = null,
            comprobanteIdBaja = comprobanteAnulado.id.toString(),
            vendedor = comprobanteAnulado.vendedor,
            provincia = comprobanteAnulado.provincia,
            localId = 0
        )
    }



}