package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class RenglonComprobante (
    @SerializedName("id")               val id: Int,
    @SerializedName("comprobante_id")  val comprobanteId: Int,
    @SerializedName("producto_id")     val productoId: Int,
    @SerializedName("descripcion")     val descripcion: String,
    @SerializedName("cantidad")        val cantidad: Double,
    @SerializedName("precio_unitario") val precioUnitario: Double,
    @SerializedName("tasa_iva")        val tasaIva: Double,
    @SerializedName("descuento")       val descuento: Double?,
    @SerializedName("total_linea")     val totalLinea: String,
    @SerializedName("producto")        val producto: Producto? // <-- nullable ahora
) {
    override fun toString(): String {
        return "RenglonComprobante(descripcion='$descripcion', cantidad=$cantidad, precioUnitario=$precioUnitario, totalLinea='$totalLinea', productoId=$productoId)"
    }

    fun copy(id: Int = this.id, comprobanteId: Int = this.comprobanteId, productoId: Int = this.productoId, descripcion: String = this.descripcion, cantidad: Double = this.cantidad, precioUnitario: Double = this.precioUnitario, tasaIva: Double = this.tasaIva, descuento: Double? = this.descuento, totalLinea: String = this.totalLinea, producto: Producto? = this.producto): RenglonComprobante {
        return RenglonComprobante(
            id,
            comprobanteId,
            productoId,
            descripcion,
            cantidad,
            precioUnitario,
            tasaIva,
            descuento,
            totalLinea,
            producto
        )
    }
}