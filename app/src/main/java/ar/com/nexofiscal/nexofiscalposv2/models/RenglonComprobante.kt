package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class RenglonComprobante (
    @SerializedName("id")               val id: Int,
    @SerializedName("comprobante_id")  val comprobanteId: Int,
    @SerializedName("producto_id")     val productoId: Int,
    @SerializedName("descripcion")     val descripcion: String,
    @SerializedName("cantidad")        val cantidad: Int,
    @SerializedName("precio_unitario") val precioUnitario: Double,
    @SerializedName("tasa_iva")        val tasaIva: Double,
    @SerializedName("descuento")       val descuento: Double?,
    @SerializedName("total_linea")     val totalLinea: String,
    @SerializedName("producto")        val producto: Producto
)