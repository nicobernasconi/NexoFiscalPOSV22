// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/LocalidadEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "localidades")
data class LocalidadEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val codigoPostal: String?,
    val provinciaId: Int?    // almacena solo el ID de Provincia
)
