// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/ProductoEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Almacenamos aquí los campos básicos de Producto y los IDs
 * de las entidades relacionadas (moneda, tasaIva, familia, etc.).
 * Para listas anidadas (stockActual, combinaciones) se requerirían TypeConverters
 * o tablas adicionales, que puedes añadir si lo necesitas.
 */
@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val id: Int,
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
    val monedaId: Int?,          // FK a MonedaEntity.id
    val tasaIvaId: Int?,         // FK a TasaIvaEntity.id
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
    val familiaId: Int?,         // FK a FamiliaEntity.id
    val agrupacionId: Int?,      // FK a AgrupacionEntity.id
    val proveedorId: Int?,       // FK a ProveedorEntity.id
    val tipoId: Int?,            // FK a TipoEntity.id
    val unidadId: Int?           // FK a UnidadEntity.id
)
