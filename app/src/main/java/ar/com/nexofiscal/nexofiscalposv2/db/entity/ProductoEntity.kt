package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

// --- CAMBIO: Se añaden serverId y syncStatus, y se ajusta la clave primaria ---
@Entity(tableName = "productos", indices = [Index(value = ["serverId"])])
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var serverId: Int?,
    var syncStatus: SyncStatus,

    val codigo: String?,
    val descripcion: String?,
    val descripcionAmpliada: String?,
    val stock: Int,
    val stockMinimo: Int,
    val stockPedido: Int,
    val codigoBarra: String?,
    val articuloActivado: Boolean?,
    val productoBalanza: Int?,
    val precio1: Double,
    val precio2: Double,
    val precio3: Double,
    val precio4: Double,
    val monedaId: Int?,
    val tasaIvaId: Int?,
    val incluyeIva: Int?,
    val impuestoInterno: Double,
    val tipoImpuestoInterno: Int,
    val precio1ImpuestoInterno: Double,
    val precio2ImpuestoInterno: Double,
    val precio3ImpuestoInterno: Double,
    val precioCosto: Double,
    val fraccionado: Int?,
    val rg5329_23: Int?,
    val activo: Int,
    val textoPanel: String?,
    val iibb: Double,
    val codigoBarra2: String?,
    val oferta: Int?,
    val margenGanancia: Double,
    val favorito: Int,
    val familiaId: Int?,
    val agrupacionId: Int?,
    val proveedorId: Int?,
    val tipoId: Int?,
    val unidadId: Int?
)