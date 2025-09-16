package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

// --- Modelos de Autenticación (Petición y Respuesta) ---
data class AfipAuthRequest(
    val environment: String = "dev",
    @SerializedName("tax_id") val taxId: String,
    val wsid: String = "wsfe",
    val cert: String,
    val key: String
)

data class AfipAuthResponse(
    val expiration: String,
    val token: String,
    val sign: String
)

// --- Estructuras Reutilizables para Peticiones ---
data class AuthPayload(
    @SerializedName("Token") val token: String,
    @SerializedName("Sign") val sign: String,
    @SerializedName("Cuit") val cuit: String
)

// --- Modelos para OBTENER ÚLTIMO COMPROBANTE ---
data class LastVoucherParams(
    @SerializedName("Auth") val auth: AuthPayload,
    @SerializedName("PtoVta") val pointOfSale: Int,
    @SerializedName("CbteTipo") val voucherType: Int
)

data class LastVoucherRequest(
    val environment: String = "dev",
    val method: String = "FECompUltimoAutorizado",
    val wsid: String = "wsfe",
    val params: LastVoucherParams
)



// --- Modelos para CREAR FACTURA (Solicitar CAE) ---
data class CreateVoucherParams(
    @SerializedName("Auth") val auth: AuthPayload,
    @SerializedName("FeCAEReq") val feCAEReq: FeCAEReq
)

data class CreateVoucherRequest(
    val environment: String = "dev",
    val method: String = "FECAESolicitar",
    val wsid: String = "wsfe",
    val params: CreateVoucherParams
)


data class LastVoucherResult(
    @SerializedName("CbteNro") val voucherNumber: Int
)
data class LastVoucherResponse(
    @SerializedName("FECompUltimoAutorizadoResult") val result: LastVoucherResult
)

// -- Modelos para la RESPUESTA de CREAR FACTURA (pueden contener éxito o error) --
data class Err(
    @SerializedName("Code") val code: Int,
    @SerializedName("Msg") val message: String
)

data class Errors(
    @SerializedName("Err") val errorDetails: List<Err>
)

data class FeCabResp(
    @SerializedName("Resultado") val resultado: String // "A" para Aprobado, "R" para Rechazado
)

data class CreateVoucherDetailResponse(
    @SerializedName("Resultado") val resultado: String,
    @SerializedName("CAE") val cae: String?, // Nulable, puede no venir en caso de error
    @SerializedName("CAEFchVto") val fechaVencimiento: String? // Nulable
)

data class FeDetResp(
    @SerializedName("FECAEDetResponse") val FECAEDetResponse: List<CreateVoucherDetailResponse>
)

data class CreateVoucherResult(
    @SerializedName("FeCabResp") val feCabResp: FeCabResp,
    @SerializedName("FeDetResp") val feDetResp: FeDetResp?, // Nulable
    @SerializedName("Errors") val errors: Errors? // Nulable
)

data class CbteAsoc(
    @SerializedName("Tipo") val tipo: Int,
    @SerializedName("PtoVta") val ptoVta: Int,
    @SerializedName("Nro") val nro: Long
)

data class CbtesAsocData(
    @SerializedName("CbteAsoc") val cbteAsoc: List<CbteAsoc>
)

data class CreateVoucherResponse(
    @SerializedName("FECAESolicitarResult") val result: CreateVoucherResult
)





// -- Modelos internos de la factura (sin cambios) --
data class FeCAEReq(
    @SerializedName("FeCabReq") val feCabReq: FeCabReq,
    @SerializedName("FeDetReq") val feDetReq: FeDetReq
)

data class FeCabReq(
    @SerializedName("CantReg") val cantReg: Int = 1,
    @SerializedName("PtoVta") val ptoVta: Int,
    @SerializedName("CbteTipo") val cbteTipo: Int
)

data class FeDetReq(
    @SerializedName("FECAEDetRequest") val fecaedetRequest: FECAEDetRequest
)

data class FECAEDetRequest(
    @SerializedName("Concepto") val concepto: Int = 1,
    @SerializedName("DocTipo") val docTipo: Int,
    @SerializedName("DocNro") val docNro: Long,
    @SerializedName("CbteDesde") val cbteDesde: Long,
    @SerializedName("CbteHasta") val cbteHasta: Long,
    @SerializedName("CbteFch") val cbteFch: String,
    @SerializedName("FchServDesde") val fchServDesde: String? = null,
    @SerializedName("FchServHasta") val fchServHasta: String? = null,
    @SerializedName("FchVtoPago") val fchVtoPago: String? = null,
    @SerializedName("ImpTotal") val impTotal: Double,
    @SerializedName("ImpTotConc") val impTotConc: Double = 0.0,
    @SerializedName("ImpNeto") val impNeto: Double,
    @SerializedName("ImpOpEx") val impOpEx: Double = 0.0,
    @SerializedName("ImpIVA") val impIVA: Double,
    @SerializedName("ImpTrib") val impTrib: Double = 0.0,
    @SerializedName("CondicionIVAReceptorId") val condicionIVAReceptorId: Int? = null,
    @SerializedName("MonId") val monId: String = "PES",
    @SerializedName("MonCotiz") val monCotiz: Int = 1,
    @SerializedName("Iva") val iva: IvaData?,
    @SerializedName("CbtesAsoc") val cbtesAsoc: CbtesAsocData? = null
)

data class IvaData(
    @SerializedName("AlicIva") val alicIva: List<AlicIva>
)

data class AlicIva(
    @SerializedName("Id") val id: Int,
    @SerializedName("BaseImp") val baseImp: Double,
    @SerializedName("Importe") val importe: Double
)