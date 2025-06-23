package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- CAMBIO: Se añade syncStatus ---
@Entity(tableName = "combinaciones")
data class CombinacionEntity(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0, // PK local para gestión interna
    var syncStatus: SyncStatus,

    val productoPrincipalId: Int,
    val subproductoId: Int,
    val cantidad: Double,
    val empresaId: Int
)