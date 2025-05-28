// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/PaisEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paises")
data class PaisEntity(
    @PrimaryKey val id: Int,
    val nombre: String?
)
