// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/TipoEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos")
data class TipoEntity(
    @PrimaryKey val id: Int,
    val numero: Int?,
    val nombre: String?
)
