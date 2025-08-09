package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.ColumnInfo

data class ProductoConStockCompleto(
    @ColumnInfo(name = "producto_id") val productoId: Int,
    @ColumnInfo(name = "codigo") val codigo: String?,
    @ColumnInfo(name = "descripcion") val descripcion: String?,
    @ColumnInfo(name = "stock_minimo") val stockMinimo: Int,
    @ColumnInfo(name = "stock_pedido") val stockPedido: Int,
    @ColumnInfo(name = "stock_actual") val stockActual: Double?
)
