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

    fun copy(id: Int? = this.id,
             codigo: String? = this.codigo,
             stockInicial: Double? = this.stockInicial,
             controlaStock: Boolean? = this.controlaStock,
             puntoPedido: Double? = this.puntoPedido,
             largo: Double? = this.largo,
             alto: Double? = this.alto,
             ancho: Double? = this.ancho,
             peso: Double? = this.peso,
             unidadId: Int? = this.unidadId,
             ubicacionId: Int? = this.ubicacionId,
             proveedoresId: Int? = this.proveedoresId,
             productoId: Int? = this.productoId,
             empresaId: Int? = this.empresaId,
             stockActual: Double? = this.stockActual,
             sucursalId: Int? = this.sucursalId): StockProducto {
        val stockProducto = StockProducto()
        stockProducto.id = id
        stockProducto.codigo = codigo
        stockProducto.stockInicial = stockInicial
        stockProducto.controlaStock = controlaStock
        stockProducto.puntoPedido = puntoPedido
        stockProducto.largo = largo
        stockProducto.alto = alto
        stockProducto.ancho = ancho
        stockProducto.peso = peso
        stockProducto.unidadId = unidadId
        stockProducto.ubicacionId = ubicacionId
        stockProducto.proveedoresId = proveedoresId
        stockProducto.productoId = productoId
        stockProducto.empresaId = empresaId
        stockProducto.stockActual = stockActual
        stockProducto.sucursalId = sucursalId
        return stockProducto
    }
}
