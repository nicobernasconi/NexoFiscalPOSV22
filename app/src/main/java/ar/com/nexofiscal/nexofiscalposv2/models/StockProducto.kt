package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para representar el stock de un producto en una sucursal.
 */
class StockProducto {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int? = null

    @SerializedName("codigo")
    var codigo: String? = null

    @SerializedName("stock_inicial")
    var stockInicial: Double? = null

    @SerializedName("controla_stock")
    var controlaStock: Boolean? = null

    @SerializedName("punto_pedido")
    var puntoPedido: Double? = null

    @SerializedName("largo")
    var largo: Double? = null

    @SerializedName("alto")
    var alto: Double? = null

    @SerializedName("ancho")
    var ancho: Double? = null

    @SerializedName("peso")
    var peso: Double? = null

    @SerializedName("unidad_id")
    var unidadId: Int? = null

    @SerializedName("ubicacion_id")
    var ubicacionId: Int? = null

    @SerializedName("proveedores_id")
    var proveedoresId: Int? = null

    @SerializedName("producto_id")
    var productoId: Int? = null

    @SerializedName("empresa_id")
    var empresaId: Int? = null

    @SerializedName("stock_actual")
    var stockActual: Double? = null

    @SerializedName("sucursal_id")
    var sucursalId: Int? = null

    override fun toString(): String {
        return "StockProducto{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", stockInicial=" + stockInicial +
                ", controlaStock=" + controlaStock +
                ", puntoPedido=" + puntoPedido +
                ", largo=" + largo +
                ", alto=" + alto +
                ", ancho=" + ancho +
                ", peso=" + peso +
                ", unidadId=" + unidadId +
                ", ubicacionId=" + ubicacionId +
                ", proveedoresId=" + proveedoresId +
                ", productoId=" + productoId +
                ", empresaId=" + empresaId +
                ", stockActual=" + stockActual +
                ", sucursalId=" + sucursalId +
                '}'
    }
}
