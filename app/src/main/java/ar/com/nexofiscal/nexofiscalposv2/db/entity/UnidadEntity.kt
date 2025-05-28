// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/UnidadEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unidades")
data class UnidadEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val simbolo: String?
)
