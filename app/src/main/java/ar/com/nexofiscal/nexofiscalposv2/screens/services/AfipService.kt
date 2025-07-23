package ar.com.nexofiscal.nexofiscalposv2.screens.services

import android.util.Base64
import android.util.Log
import ar.com.nexofiscal.nexofiscalposv2.managers.AfipAuthManager
import ar.com.nexofiscal.nexofiscalposv2.managers.AfipVoucherManager
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.models.Comprobante
import ar.com.nexofiscal.nexofiscalposv2.ui.LoadingManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Servicio para manejar toda la lógica de facturación electrónica con AFIP
 */
object AfipService {

    private const val TAG = "AfipService"

    /**
     * Procesa una factura electrónica y obtiene el CAE de AFIP
     */
    suspend fun procesarFacturaElectronica(
        comprobante: Comprobante,
        clienteSeleccionado: Cliente?
    ): Comprobante {
        LoadingManager.show()

        try {
            // 1. VALIDAR CREDENCIALES DE AFIP
            val credenciales = validarCredencialesAfip()

            Log.d(TAG, "Iniciando proceso de facturación electrónica...")
            Log.d(TAG, "CUIT: ${credenciales.cuit}, Punto de Venta: ${credenciales.puntoVenta}")

            // 2. AUTENTICACIÓN CON AFIP SDK
            val authResponse = AfipAuthManager.getAuthToken(
                credenciales.cuit,
                credenciales.cert,
                credenciales.key
            ) ?: throw Exception("Fallo en la autenticación con AFIP. Verifique las credenciales y la conectividad.")

            Log.d(TAG, "Token obtenido con éxito.")

            // 3. DETERMINAR TIPO DE FACTURA Y LETRA
            val (tipoFacturaAfip, letraFactura) = determinarTipoFactura(clienteSeleccionado)

            Log.d(TAG, "Tipo factura AFIP: $tipoFacturaAfip, Letra: $letraFactura")

            // 4. OBTENER EL ÚLTIMO NÚMERO DE COMPROBANTE + 1
            val proximoNumero = obtenerProximoNumero(authResponse, credenciales, tipoFacturaAfip)

            Log.d(TAG, "Próximo número de factura: $proximoNumero")

            // 5. ACTUALIZAR EL COMPROBANTE CON DATOS FISCALES
            val comprobanteActualizado = comprobante.copy(
                tipoFactura = tipoFacturaAfip,
                letra = letraFactura,
                numeroFactura = proximoNumero,
                puntoVenta = credenciales.puntoVenta
            )

            // 6. CREAR LA FACTURA ELECTRÓNICA Y SOLICITAR CAE
            val caeResponse = AfipVoucherManager.createElectronicVoucher(
                auth = authResponse,
                cuit = credenciales.cuit,
                comprobante = comprobanteActualizado
            )

            Log.d(TAG, "CAE: ${caeResponse.cae}, Vencimiento: ${caeResponse.fechaVencimiento}")

            // 7. ACTUALIZAR EL COMPROBANTE FINAL CON CAE Y VENCIMIENTO
            val comprobanteFinal = comprobanteActualizado.copy(
                cae = caeResponse.cae,
                fechaVencimiento = caeResponse.fechaVencimiento,
                qr = generarCodigoQR(credenciales, tipoFacturaAfip, proximoNumero, comprobanteActualizado, caeResponse.cae, clienteSeleccionado)
            )

            Log.d(TAG, "Factura electrónica procesada exitosamente")
            return comprobanteFinal

        } catch (e: Exception) {
            Log.e(TAG, "Error en el flujo de facturación electrónica: ${e.message}")
            NotificationManager.show(e.message ?: "Ocurrió un error desconocido con AFIP.", NotificationType.ERROR)
            throw e
        } finally {
            LoadingManager.hide()
        }
    }

    /**
     * Valida que las credenciales de AFIP estén configuradas
     */
    private fun validarCredencialesAfip(): CredencialesAfip {
        val cuit = SessionManager.empresaCuit?.replace("-", "") ?: ""
        val cert = SessionManager.certificadoAfip ?: ""
        val key = SessionManager.claveAfip ?: ""
        val puntoVenta = SessionManager.puntoVentaNumero

        when {
            cuit.isBlank() -> throw Exception("CUIT de la empresa no configurado. Configure las credenciales AFIP.")
            cert.isBlank() -> throw Exception("Certificado AFIP no configurado. Configure las credenciales AFIP.")
            key.isBlank() -> throw Exception("Clave privada AFIP no configurada. Configure las credenciales AFIP.")
            puntoVenta <= 0 -> throw Exception("Punto de venta no configurado correctamente.")
        }

        return CredencialesAfip(cuit, cert, key, puntoVenta)
    }

    /**
     * Determina el tipo de factura y letra según las condiciones de IVA
     */
    private fun determinarTipoFactura(clienteSeleccionado: Cliente?): Pair<Int, String> {
        val idIvaVendedor = SessionManager.empresaTipoIva
        val condicionIvaCliente = clienteSeleccionado?.tipoIva?.nombre?.uppercase(Locale.ROOT) ?: "CONSUMIDOR"

        Log.d(TAG, "Tipo IVA vendedor: $idIvaVendedor, Condición IVA cliente: $condicionIvaCliente")

        return when (idIvaVendedor) {
            1 -> { // Vendedor es Responsable Inscripto
                if (condicionIvaCliente.contains("INSCRIPTO")) 1 to "A" else 6 to "B"
            }
            2 -> { // Vendedor es Monotributista
                11 to "C"
            }
            3 -> { // Vendedor es Exento
                6 to "B"
            }
            else -> {
                throw Exception("Tipo de IVA del vendedor no válido: $idIvaVendedor")
            }
        }
    }

    /**
     * Obtiene el próximo número de comprobante de AFIP
     */
    private suspend fun obtenerProximoNumero(
        authResponse: ar.com.nexofiscal.nexofiscalposv2.models.AfipAuthResponse,
        credenciales: CredencialesAfip,
        tipoFacturaAfip: Int
    ): Int {
        val ultimoNumero = AfipVoucherManager.getLastVoucherNumber(
            auth = authResponse,
            cuit = credenciales.cuit,
            pointOfSale = credenciales.puntoVenta,
            voucherType = tipoFacturaAfip
        ) ?: throw Exception("No se pudo obtener el próximo número de factura de AFIP.")

        return ultimoNumero + 1
    }

    /**
     * Genera el código QR para la factura electrónica
     */
    private fun generarCodigoQR(
        credenciales: CredencialesAfip,
        tipoFacturaAfip: Int,
        proximoNumero: Int,
        comprobante: Comprobante,
        cae: String?,
        clienteSeleccionado: Cliente?
    ): String {
        val (tipoDocumentoAfip, numeroDocumentoAfip) = obtenerDatosDocumento(clienteSeleccionado)

        val afipJson = JSONObject().apply {
            put("ver", 1)
            put("fecha", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            put("cuit", credenciales.cuit.toLong())
            put("ptoVta", credenciales.puntoVenta)
            put("tipoCmp", tipoFacturaAfip)
            put("nroCmp", proximoNumero)
            put("importe", comprobante.total?.toDoubleOrNull() ?: 0.0)
            put("moneda", "PES")
            put("ctz", 1)
            put("tipoDocRec", tipoDocumentoAfip)
            put("nroDocRec", numeroDocumentoAfip)
            put("tipoCodAut", "E")
            put("codAut", cae)
        }

        val jsonString = afipJson.toString()
        val base64String = Base64.encodeToString(jsonString.toByteArray(), Base64.NO_WRAP)
        return "https://www.afip.gob.ar/fe/qr/?p=$base64String"
    }

    /**
     * Obtiene los datos del documento del cliente para AFIP
     */
    fun obtenerDatosDocumento(clienteSeleccionado: Cliente?): Pair<Int, Long> {
        var tipoDocumentoAfip = 99 // Por defecto: Consumidor Final sin identificar
        var numeroDocumentoAfip = 0L // Por defecto: 0

        val condicionIvaCliente = clienteSeleccionado?.tipoIva?.nombre?.uppercase(Locale.ROOT) ?: "CONSUMIDOR"

        if (clienteSeleccionado != null) {
            when {
                condicionIvaCliente.contains("INSCRIPTO") ||
                        condicionIvaCliente.contains("MONOTRIBUTO") ||
                        condicionIvaCliente.contains("EXENTO") -> {
                    tipoDocumentoAfip = 80 // CUIT
                    numeroDocumentoAfip = clienteSeleccionado.cuit?.replace("-", "")?.toLongOrNull() ?: 0L
                }
                condicionIvaCliente.contains("CONSUMIDOR") -> {
                    tipoDocumentoAfip = 96 // DNI
                    numeroDocumentoAfip = clienteSeleccionado.numeroDocumento?.toLongOrNull() ?: 0L
                }
            }
        }

        return tipoDocumentoAfip to numeroDocumentoAfip
    }

    /**
     * Clase de datos para las credenciales de AFIP
     */
    private data class CredencialesAfip(
        val cuit: String,
        val cert: String,
        val key: String,
        val puntoVenta: Int
    )
}
