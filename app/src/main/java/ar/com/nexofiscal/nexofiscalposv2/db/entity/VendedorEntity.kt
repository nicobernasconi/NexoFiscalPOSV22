// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/VendedorEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vendedores")
data class VendedorEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val direccion: String?,
    val telefono: String?,
    val porcentajeComision: Double?,
    val fechaIngreso: String?
)
