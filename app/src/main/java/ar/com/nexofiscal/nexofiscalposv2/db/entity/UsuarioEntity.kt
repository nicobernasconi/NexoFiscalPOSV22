package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// --- CAMBIO: Se añaden serverId y syncStatus, y se ajusta la clave primaria ---
@Entity(tableName = "usuarios", indices = [Index(value = ["serverId"])])
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var serverId: Int?,
    var syncStatus: SyncStatus,

    val nombreUsuario: String?,
    val nombreCompleto: String?,
    val rolId: Int?,
    val sucursalId: Int?,
    val vendedorId: Int?,
    val email: String?
)