package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// --- CAMBIO: Se añaden serverId y syncStatus, y se ajusta la clave primaria ---
@Entity(tableName = "formas_pago", indices = [Index(value = ["serverId"])])
data class FormaPagoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var serverId: Int?,
    var syncStatus: SyncStatus,
    val nombre: String?,
    val porcentaje: Int,
    val activa: Int = 1,
    val tipoFormaPagoId: Int?
)