// main/java/ar/com/nexofiscal/nexofiscalposv2/models/FormaPagoComprobante.kt
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

data class FormaPagoComprobante(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("porcentaje") val porcentaje: Int,
    @SerializedName("importe") val importe: String,
    @SerializedName("tipo_forma_pago") val tipoFormaPago: TipoFormaPago
)