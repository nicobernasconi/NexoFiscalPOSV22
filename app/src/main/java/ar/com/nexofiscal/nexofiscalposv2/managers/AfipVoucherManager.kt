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
            if ((comprobante.importeIva21 ?: 0.0) > 0) AlicIva(id = 5, baseImp = (comprobante.noGravadoIva21 ?: 0.0), importe = (comprobante.importeIva21 ?: 0.0)) else null,
            if ((comprobante.importeIva105 ?: 0.0) > 0) AlicIva(id = 4, baseImp = (comprobante.noGravadoIva105 ?: 0.0), importe = (comprobante.importeIva105 ?: 0.0)) else null,
            if ((comprobante.noGravadoIva0 ?: 0.0) > 0) AlicIva(id = 3, baseImp = (comprobante.noGravadoIva0 ?: 0.0), importe = 0.0) else null
        ).filterNotNull()

        // Condición IVA receptor: depende del tipo de contribuyente (cliente)
        val idInferido = inferCondicionIvaId(comprobante.cliente?.tipoIva?.nombre)
        val condicionIvaReceptorId = idInferido ?: run {
            when (comprobante.tipoDocumento) {
                96 -> 5 // DNI -> Consumidor Final
                80 -> { // CUIT
                    val nombre = comprobante.cliente?.tipoIva?.nombre?.uppercase(Locale.getDefault())
                    when {
                        nombre?.contains("MONOTRIBUTO") == true -> 5
                        nombre?.contains("EXENTO") == true -> 4
                        nombre?.contains("INSCRIPTO") == true -> 1
                        else -> 5
                    }
                }
                else -> 5
            }
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
    ): Pair<CreateVoucherDetailResponse, Int> { // devuelve detalle y número asignado
        val puntoDeVenta = comprobanteAnulado.puntoVenta!!
        // Mapear tipo factura original a tipo de nota de crédito
        val tipoNotaDeCredito = when (comprobanteAnulado.tipoFactura) {
            1 -> 3   // Factura A -> NC A
            6 -> 8   // Factura B -> NC B
            11 -> 13 // Factura C -> NC C
            51 -> 53 // Factura M -> NC M
            else -> throw IllegalArgumentException("Tipo de factura no soportado para NC: ${comprobanteAnulado.tipoFactura}")
        }

        // Obtener último número autorizado para la NC correspondiente
        val lastVoucher = getLastVoucherNumber(auth, cuit, puntoDeVenta, tipoNotaDeCredito)
        val numeroDeNota = (lastVoucher ?: 0) + 1

        val fechaActual = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        // Nuevo: inferir y normalizar condición IVA receptor compatible con el tipo de NC
        val idInferido = inferCondicionIvaId(comprobanteAnulado.cliente?.tipoIva?.nombre)
        val condicionIvaReceptorId = normalizarCondicionIvaParaNC(
            tipoNC = tipoNotaDeCredito,
            idInferido = idInferido,
            docTipo = comprobanteAnulado.tipoDocumento,
            cliente = comprobanteAnulado.cliente
        )

        val base21 = comprobanteAnulado.noGravadoIva21 ?: 0.0
        val base105 = comprobanteAnulado.noGravadoIva105 ?: 0.0
        val base0 = comprobanteAnulado.noGravadoIva0 ?: 0.0
        val iva21 = comprobanteAnulado.importeIva21 ?: 0.0
        val iva105 = comprobanteAnulado.importeIva105 ?: 0.0
        val iva0 = comprobanteAnulado.importeIva0 ?: 0.0 // normalmente 0

        val subtotalesIva = listOf(
            if (iva21 > 0) AlicIva(id = 5, baseImp = base21, importe = iva21) else null,
            if (iva105 > 0) AlicIva(id = 4, baseImp = base105, importe = iva105) else null,
            if (base0 > 0) AlicIva(id = 3, baseImp = base0, importe = 0.0) else null
        ).filterNotNull()

        // Recalcular importes coherentes para AFIP
        val impNeto = (base21 + base105 + base0)
        val impIVA = (iva21 + iva105 + iva0)
        val impTotalCalculado = impNeto + impIVA
        val impTotalOriginal = comprobanteAnulado.total?.toDoubleOrNull() ?: impTotalCalculado
        if (kotlin.math.abs(impTotalCalculado - impTotalOriginal) > 0.05) {
            Log.w(TAG, "Ajuste impTotal NC: original=${impTotalOriginal} vs calculado=${impTotalCalculado}. Se usará calculado.")
        }
        val impTotal = if (impTotalOriginal <= 0.0) impTotalCalculado else impTotalCalculado

        val feCabReq = FeCabReq(ptoVta = puntoDeVenta, cbteTipo = tipoNotaDeCredito)

        val feDetRequest = FECAEDetRequest(
            docTipo = comprobanteAnulado.tipoDocumento!!,
            docNro = comprobanteAnulado.numeroDeDocumento!!,
            cbteDesde = numeroDeNota.toLong(),
            cbteHasta = numeroDeNota.toLong(),
            cbteFch = fechaActual,
            impTotal = impTotal,
            impNeto = impNeto,
            impIVA = impIVA,
            iva = if (subtotalesIva.isNotEmpty()) IvaData(subtotalesIva) else null,
            condicionIVAReceptorId = condicionIvaReceptorId,
            cbtesAsoc = CbtesAsocData(
                cbteAsoc = listOf(
                    CbteAsoc(
                        tipo = comprobanteAnulado.tipoFactura!!,
                        ptoVta = comprobanteAnulado.puntoVenta!!,
                        nro = comprobanteAnulado.numeroFactura!!.toLong()
                    )
                )
            )
        )

        val feCAEReq = FeCAEReq(feCabReq, FeDetReq(feDetRequest))
        val params = CreateVoucherParams(auth = AuthPayload(auth.token, auth.sign, cuit), feCAEReq = feCAEReq)
        val request = CreateVoucherRequest(params = params)

        Log.d(TAG, "Creando Nota de Crédito N°$numeroDeNota (tipo $tipoNotaDeCredito). Payload: ${Gson().toJson(request)}")

        val detail = withContext(Dispatchers.IO) {
            try {
                val response = AfipApiClient.instance.createVoucher(request).execute()
                if (response.isSuccessful) {
                    val bodyRaw = response.body()
                    val afipResult = bodyRaw?.result
                    Log.d(TAG, "Respuesta bruta NC: ${Gson().toJson(bodyRaw)}")
                    if (afipResult?.feCabResp?.resultado == "R") {
                        val errorMsg = afipResult.errors?.errorDetails?.joinToString(" | ") { "${it.code}:${it.message}" }
                            ?: "AFIP rechazó la solicitud de NC sin mensaje claro."
                        throw Exception(errorMsg)
                    }
                    val detailResponse = afipResult?.feDetResp?.FECAEDetResponse?.firstOrNull()
                    if (detailResponse?.resultado == "A" && !detailResponse.cae.isNullOrBlank()) {
                        Log.i(TAG, "NC CAE obtenido: ${detailResponse.cae}")
                        detailResponse
                    } else {
                        throw Exception("Respuesta AFIP aprobada pero sin CAE válido para la NC.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error HTTP al crear NC. Código: ${response.code()}, Mensaje: $errorBody")
                    throw Exception("Error comunicación AFIP (${response.code()})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al crear NC: ${e.message}", e)
                throw e
            }
        }
        return detail to numeroDeNota
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

        // Funciones helper para evitar NPE
        fun dbl(v: Double?): Double = v ?: 0.0
        fun strTotal(v: String?): String = (v?.toDoubleOrNull() ?: 0.0).toString()

        return Comprobante(
            id = 0,
            serverId = null,
            cliente = comprobanteAnulado.cliente,
            clienteId = comprobanteAnulado.clienteId,
            tipoComprobante = comprobanteAnulado.tipoComprobante,
            tipoComprobanteId = 4,
            fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
            total = strTotal(comprobanteAnulado.total),
            totalPagado = comprobanteAnulado.totalPagado,
            descuentoTotal = "0.0",
            incrementoTotal = "0.0",
            importeIva = dbl(comprobanteAnulado.importeIva),
            noGravado = dbl(comprobanteAnulado.noGravado),
            importeIva21 = dbl(comprobanteAnulado.importeIva21),
            importeIva105 = dbl(comprobanteAnulado.importeIva105),
            importeIva0 = dbl(comprobanteAnulado.importeIva0),
            noGravadoIva21 = dbl(comprobanteAnulado.noGravadoIva21),
            noGravadoIva105 = dbl(comprobanteAnulado.noGravadoIva105),
            noGravadoIva0 = dbl(comprobanteAnulado.noGravadoIva0),
            numero = null,
            puntoVenta = comprobanteAnulado.puntoVenta,
            empresaId = comprobanteAnulado.empresaId,
            sucursalId = comprobanteAnulado.sucursalId,
            vendedorId = comprobanteAnulado.vendedorId,
            formas_de_pago = emptyList(),
            promociones = emptyList(),
            cuotas = null,
            remito = null,
            persona = null,
            provinciaId = comprobanteAnulado.provinciaId,
            fechaBaja = null,
            motivoBaja = null,
            fechaProceso = null,
            letra = comprobanteAnulado.letra,
            numeroFactura = null,
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
            tipoFactura = tipoNotaDeCredito,
            tipoDocumento = comprobanteAnulado.tipoDocumento,
            numeroDeDocumento = comprobanteAnulado.numeroDeDocumento,
            qr = null,
            comprobanteIdBaja = (comprobanteAnulado.serverId ?: comprobanteAnulado.id).toString(),
            vendedor = comprobanteAnulado.vendedor,
            provincia = comprobanteAnulado.provincia,
            localId = 0
        )
    }

    // Helper: infiere id de condición de IVA a partir del nombre de la condición
    private fun inferCondicionIvaId(nombreCondicion: String?): Int? {
        val n = nombreCondicion?.uppercase(Locale.getDefault()) ?: return null
        return when {
            n.contains("INSCRIPTO") -> 1
            n.contains("NO INSCRIPTO") -> 2
            n.contains("NO RESPONSABLE") -> 3
            n.contains("EXENTO") -> 4
            n.contains("CONSUMIDOR") -> 5
            n.contains("MONOTRIBUTO") -> 5
            n.contains("NO CATEGORIZADO") -> 7
            n.contains("PROVEEDOR DEL EXTERIOR") -> 8
            n.contains("CLIENTE DEL EXTERIOR") -> 9
            n.contains("LIBERADO") -> 10
            else -> null
        }
    }

    // Helper: normaliza id de condición de IVA según el tipo de NC (compatibilidad AFIP)
    private fun normalizarCondicionIvaParaNC(
        tipoNC: Int,
        idInferido: Int?,
        docTipo: Int?,
        cliente: Cliente?
    ): Int {
        val id = idInferido ?: 5 // default CF
        return when (tipoNC) {
            3, 53 -> { // NC A o NC M
                // Para clases A/M, evitar CF/Monotributo; forzar RI si no coincide
                if (id in setOf(1, 11)) 1 else 1
            }
            8 -> { // NC B
                // Permitidos típicos: Exento(4), CF(5), Monotributo(6), No categ(7)
                if (id in setOf(4, 5, 6, 7)) id
                else {
                    // Elegir heurística según documento/condición
                    when {
                        cliente?.tipoIva?.nombre?.uppercase(Locale.getDefault())?.contains("MONOTRIBUTO") == true -> 5
                        cliente?.tipoIva?.nombre?.uppercase(Locale.getDefault())?.contains("EXENTO") == true -> 4
                        docTipo == 96 -> 5
                        else -> 5
                    }
                }
            }
            13 -> { // NC C
                // Generalmente aceptan varias; usar inferido si está, sino CF
                id
            }
            else -> id
        }
    }



}
