package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

/**
 * DTO para la subida de un único Renglón de Comprobante a la API.
 */
data class RenglonUploadRequest(
    @SerializedName("producto_id") val productoId: Int,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("cantidad") val cantidad: Double,
    @SerializedName("precio_unitario") val precioUnitario: Double,
    @SerializedName("tasa_iva") val tasaIva: Double,
    @SerializedName("total_linea") val totalLinea: String
)