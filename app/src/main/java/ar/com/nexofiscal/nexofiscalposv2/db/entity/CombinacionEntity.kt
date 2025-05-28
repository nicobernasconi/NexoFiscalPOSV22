// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/CombinacionEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla para combinaciones de productos.
 * La clave primaria puede ser compuesta, pero aquí usamos un autogenerado.
 */
@Entity(tableName = "combinaciones")
data class CombinacionEntity(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,                  // PK interno
    val productoPrincipalId: Int,
    val subproductoId: Int,
    val cantidad: Double,
    val empresaId: Int
)
