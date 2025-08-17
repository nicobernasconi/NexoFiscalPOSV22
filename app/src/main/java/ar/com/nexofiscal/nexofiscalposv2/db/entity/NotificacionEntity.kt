package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "notificaciones", indices = [Index(value = ["serverId"])])
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var serverId: Int?,
    var syncStatus: SyncStatus,

    val nombre: String?,
    val mensaje: String?,
    val empresaId: Int?,
    val activo: Int?,
    val tipoNotificacionId: Int?
)
