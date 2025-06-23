package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// --- CAMBIO: Se añaden serverId y syncStatus, y se hace el id local autoincremental ---
@Entity(tableName = "agrupaciones", indices = [Index(value = ["serverId"])])
data class AgrupacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var serverId: Int?,
    var syncStatus: SyncStatus,

    val numero: Int?,
    val nombre: String?,
    val color: String?,
    val icono: String?
)