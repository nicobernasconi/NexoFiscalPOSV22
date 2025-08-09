package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.ColumnInfo

data class ProductoConStockCompleto(
    val productoId: Int,
    val codigo: String?,
    val descripcion: String?,
    val stockActual: Double?, // De StockProductoEntity
    val stockMinimo: Int,     // De ProductoEntity
    val stockPedido: Int      // De ProductoEntity
)