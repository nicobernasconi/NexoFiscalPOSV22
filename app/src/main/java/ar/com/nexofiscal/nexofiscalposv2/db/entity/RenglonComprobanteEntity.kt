package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Guardamos cada RenglonComprobante serializado en JSON en `data`.
 */
@Entity(
    tableName = "renglones_comprobante",
    // CAMBIO: Se añade una clave foránea para garantizar la integridad referencial.
    foreignKeys = [
        ForeignKey(
            entity = ComprobanteEntity::class,
            parentColumns = ["id"], // Clave primaria local de ComprobanteEntity
            childColumns = ["comprobanteLocalId"], // Nueva clave foránea en esta tabla
            onDelete = ForeignKey.CASCADE // Si se borra el comprobante, se borran sus renglones.
        )
    ],
    // CAMBIO: Se añade un índice sobre la nueva columna para optimizar consultas.
    indices = [Index(value = ["comprobanteLocalId"])]
)
data class RenglonComprobanteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val comprobanteLocalId: Int,
    val data: String
)
