// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/TipoIvaEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_iva")
data class TipoIvaEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val letraFactura: String?,
    val porcentaje: Double?
)
