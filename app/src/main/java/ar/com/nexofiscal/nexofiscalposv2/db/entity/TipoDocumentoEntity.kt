// 1. Entity Room
// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/TipoDocumentoEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_documento")
data class TipoDocumentoEntity(
    @PrimaryKey val id: Int,
    val nombre: String?
)
