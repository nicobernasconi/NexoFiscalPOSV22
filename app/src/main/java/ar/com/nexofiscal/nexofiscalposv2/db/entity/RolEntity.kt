// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/RolEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roles")
data class RolEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val descripcion: String?
)
