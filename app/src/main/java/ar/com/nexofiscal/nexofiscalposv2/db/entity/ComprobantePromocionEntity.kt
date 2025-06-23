package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "comprobante_promociones",
    primaryKeys = ["comprobanteLocalId", "promocionId"],
    foreignKeys = [
        ForeignKey(
            entity = ComprobanteEntity::class,
            parentColumns = ["id"],
            childColumns = ["comprobanteLocalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["comprobanteLocalId"])]
)
data class ComprobantePromocionEntity(
    val comprobanteLocalId: Long,
    val promocionId: Int
)