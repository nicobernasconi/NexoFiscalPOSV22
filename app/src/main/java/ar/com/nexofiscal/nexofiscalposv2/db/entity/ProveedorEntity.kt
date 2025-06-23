package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// --- CAMBIO: Se añaden serverId y syncStatus, y se ajusta la clave primaria ---
@Entity(tableName = "proveedores", indices = [Index(value = ["serverId"])])
data class ProveedorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var serverId: Int?,
    var syncStatus: SyncStatus,

    val razonSocial: String?,
    val direccion: String?,
    val localidadId: Int?,
    val telefono: String?,
    val email: String?,
    val tipoIvaId: Int?,
    val cuit: String?,
    val categoriaId: Int?,
    val subcategoriaId: Int?,
    val fechaUltimaCompra: String?,
    val fechaUltimoPago: String?,
    val saldoActual: Double
)