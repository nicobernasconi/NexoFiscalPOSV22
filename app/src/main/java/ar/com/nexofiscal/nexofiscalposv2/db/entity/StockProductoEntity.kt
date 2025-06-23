package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// --- CAMBIO: Se añaden serverId y syncStatus, y se ajusta la clave primaria ---
@Entity(tableName = "stock_productos", indices = [Index(value = ["serverId"])])
data class StockProductoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var serverId: Int?,
    var syncStatus: SyncStatus,

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