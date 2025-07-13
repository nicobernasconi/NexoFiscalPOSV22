package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

// Estructura principal del request
data class AfipWsRequest(
    val environment: String = "dev",
    val method: String = "FECAESolicitar",
    val wsid: String = "wsfe",
    val params: Params
)

// Parámetros que contienen la autenticación y el cuerpo de la factura
data class Params(
    @SerializedName("Auth")
    val auth: Auth,

    @SerializedName("FeCAEReq")
    val feCAEReq: FeCAEReq
)

// Objeto de autenticación
data class Auth(
    @SerializedName("Token")
    val token: String,

    @SerializedName("Sign")
    val sign: String,

    @SerializedName("Cuit")
    val cuit: String
)

// Cuerpo principal de la solicitud de CAE
data class FeCAEReq(
    @SerializedName("FeCabReq")
    val feCabReq: FeCabReq,

    @SerializedName("FeDetReq")
    val feDetReq: FeDetReq
)

// Cabecera de la solicitud
data class FeCabReq(
    @SerializedName("CantReg")
    val cantReg: Int = 1,

    @SerializedName("PtoVta")
    val ptoVta: Int,

    @SerializedName("CbteTipo")
    val cbteTipo: Int
)

// Array de detalles (aunque en los ejemplos siempre es uno)
data class FeDetReq(
    @SerializedName("FECAEDetRequest")
    val fecaedetRequest: FECAEDetRequest
)

// El detalle de la factura
data class FECAEDetRequest(
    @SerializedName("Concepto")
    val concepto: Int = 1, // 1: Productos, 2: Servicios, 3: Productos y Servicios

    @SerializedName("DocTipo")
    val docTipo: Int,

    @SerializedName("DocNro")
    val docNro: Long,

    @SerializedName("CbteDesde")
    val cbteDesde: Long,

    @SerializedName("CbteHasta")
    val cbteHasta: Long,

    @SerializedName("CbteFch")
    val cbteFch: String, // Formato YYYYMMDD

    @SerializedName("ImpTotal")
    val impTotal: Double,

    @SerializedName("ImpTotConc")
    val impTotConc: Double = 0.0,

    @SerializedName("ImpNeto")
    val impNeto: Double,

    @SerializedName("ImpOpEx")
    val impOpEx: Double = 0.0,

    @SerializedName("ImpIVA")
    val impIVA: Double,

    @SerializedName("ImpTrib")
    val impTrib: Double = 0.0,

    @SerializedName("MonId")
    val monId: String = "PES",

    @SerializedName("MonCotiz")
    val monCotiz: Int = 1,

    @SerializedName("Iva")
    val iva: IvaData?, // Puede ser nulo para Factura C

    @SerializedName("CbtesAsoc")
    val cbtesAsoc: CbtesAsoc? = null // Para notas de crédito/débito
)

// Contenedor para los subtotales de IVA
data class IvaData(
    @SerializedName("AlicIva")
    val alicIva: List<AlicIva>
)

// Detalle de cada alícuota de IVA
data class AlicIva(
    @SerializedName("Id")
    val id: Int, // 5: 21%, 4: 10.5%, 3: 0%

    @SerializedName("BaseImp")
    val baseImp: Double,

    @SerializedName("Importe")
    val importe: Double
)

// Contenedor para los comprobantes asociados
data class CbtesAsoc(
    @SerializedName("CbteAsoc")
    val cbteAsoc: List<CbteAsoc>
)

// Detalle de cada comprobante asociado
data class CbteAsoc(
    @SerializedName("Tipo")
    val tipo: Int,

    @SerializedName("PtoVta")
    val ptoVta: Int,

    @SerializedName("Nro")
    val nro: Long
)