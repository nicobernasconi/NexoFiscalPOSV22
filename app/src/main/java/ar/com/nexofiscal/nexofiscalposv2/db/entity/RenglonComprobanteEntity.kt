// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/RenglonComprobanteEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Guardamos cada RenglonComprobante serializado en JSON en `data`.
 */
@Entity(tableName = "renglones_comprobante")
data class RenglonComprobanteEntity(
    @PrimaryKey val id: Int,
    val comprobanteId: Int,
    val data: String
)
