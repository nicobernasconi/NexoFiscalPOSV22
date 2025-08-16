package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "stock_actualizaciones",
    indices = [
        Index(value = ["productoId", "sucursalId"]),
        Index(value = ["fechaCreacion"])
    ]
)
data class StockActualizacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val productoId: Int,
    val sucursalId: Int,
    val cantidad: Double,
    val fechaCreacion: Date = Date(),
    val enviado: Boolean = false,
    val fechaEnvio: Date? = null,
    val intentos: Int = 0,
    val ultimoError: String? = null
)
