// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/FamiliaEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "familias")
data class FamiliaEntity(
    @PrimaryKey val id: Int,
    val numero: Int?,
    val nombre: String?
)
