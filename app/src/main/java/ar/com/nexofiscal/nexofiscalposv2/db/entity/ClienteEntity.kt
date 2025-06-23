// main/java/ar/com/nexofiscal/nexofiscalposv2/db/entity/ClienteEntity.kt

package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "clientes", indices = [Index(value = ["serverId"], unique = true)]) // `unique = true` es una buena práctica
data class ClienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Clave local
    var serverId: Int?, // <-- CAMBIO: AÑADIDO
    var syncStatus: SyncStatus, // <-- CAMBIO: AÑADIDO

    val nroCliente: Int,
    val nombre: String?,
    val cuit: String?,
    // ... el resto de los campos permanece igual
    val tipoDocumentoId: Int?,
    val numeroDocumento: String?,
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