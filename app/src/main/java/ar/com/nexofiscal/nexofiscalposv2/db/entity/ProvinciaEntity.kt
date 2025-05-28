// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/ProvinciaEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provincias")
data class ProvinciaEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val paisId: Int?    // FK a PaisEntity.id
)
