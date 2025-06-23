package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// --- CAMBIO: Se añaden serverId y syncStatus ---
@Entity(tableName = "cierres_caja", indices = [Index(value = ["serverId"])])
data class CierreCajaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var serverId: Int?,
    var syncStatus: SyncStatus,

    val fecha: String?,
    val totalVentas: Double?,
    val totalGastos: Double?,
    val efectivoInicial: Double?,
    val efectivoFinal: Double?,
    val tipoCajaId: Int?,
    val usuarioId: Int?
)