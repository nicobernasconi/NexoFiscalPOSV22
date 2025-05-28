// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/TipoComprobanteEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_comprobante")
data class TipoComprobanteEntity(
    @PrimaryKey val id: Int,
    val numero: Int?,
    val nombre: String?
)
