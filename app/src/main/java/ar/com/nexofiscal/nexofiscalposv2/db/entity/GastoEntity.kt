package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "gastos",
    indices = [Index(value = ["serverId"]), Index(value = ["tipoGastoId"])],
    foreignKeys = [
        ForeignKey(
            entity = TipoGastoEntity::class,
            parentColumns = ["id"],
            childColumns = ["tipoGastoId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION
        )
    ]
)
data class GastoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var serverId: Int?,
    var syncStatus: SyncStatus,

    val descripcion: String?,
    val monto: Double?,
    val fecha: String?,
    val usuarioId: Int?,
    val empresaId: Int?,
    val tipoGastoId: Int?,
    val tipoGasto: String?,
    val cierreCajaId: Int?
)
