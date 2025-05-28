// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/ClienteEntity.kt
package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val id: Int,
    val nroCliente: Int,
    val nombre: String?,
    val cuit: Long?,
    val tipoDocumentoId: Int?,
    val numeroDocumento: Long?,
    val direccionComercial: String?,
    val direccionEntrega: String?,
    val localidadId: Int?,
    val telefono: String?,
    val celular: String?,
    val email: String?,
    val contacto: String?,
    val telefonoContacto: String?,
    val categoriaId: Int?,
    val vendedoresId: Int?,
    val porcentajeDescuento: Double?,
    val limiteCredito: Double?,
    val saldoInicial: Double?,
    val saldoActual: Double?,
    val fechaUltimaCompra: String?,
    val fechaUltimoPago: String?,
    val percepcionIibb: Double?,
    val desactivado: Boolean?,
    val tipoIvaId: Int?,
    val provinciaId: Int?
)
