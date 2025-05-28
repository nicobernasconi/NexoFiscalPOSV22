// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/StockProductoEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_productos")
data class StockProductoEntity(
    @PrimaryKey val id: Int,
    val codigo: String?,
    val stockInicial: Double?,
    val controlaStock: Boolean?,
    val puntoPedido: Double?,
    val largo: Double?,
    val alto: Double?,
    val ancho: Double?,
    val peso: Double?,
    val unidadId: Int?,
    val ubicacionId: Int?,
    val proveedoresId: Int?,
    val productoId: Int?,
    val empresaId: Int?,
    val stockActual: Double?,
    val sucursalId: Int?
)
