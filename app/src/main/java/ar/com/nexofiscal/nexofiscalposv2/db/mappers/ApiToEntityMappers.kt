// File: main/java/ar/com/nexofiscal/nexofiscalposv2/db/mappers/ApiToEntityMappers.kt
package ar.com.nexofiscal.nexofiscalposv2.db.mappers

import ar.com.nexofiscal.nexofiscalposv2.models.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.*

// --- Cliente Mapper ---
fun Cliente.toEntity(): ClienteEntity {
    return ClienteEntity(
        id = this.id,
        nroCliente = this.nroCliente,
        nombre = this.nombre,
        cuit = this.cuit,
        tipoDocumentoId = this.tipoDocumento?.id,
        numeroDocumento = this.numeroDocumento,
        direccionComercial = this.direccionComercial,
        direccionEntrega = this.direccionEntrega,
        localidadId = this.localidad?.id,
        telefono = this.telefono,
        celular = this.celular,
        email = this.email,
        contacto = this.contacto,
        telefonoContacto = this.telefonoContacto,
        categoriaId = this.categoria?.id,
        vendedoresId = this.vendedores?.id,
        porcentajeDescuento = this.porcentajeDescuento,
        limiteCredito = this.limiteCredito,
        saldoInicial = this.saldoInicial,
        saldoActual = this.saldoActual,
        fechaUltimaCompra = this.fechaUltimaCompra,
        fechaUltimoPago = this.fechaUltimoPago,
        percepcionIibb = this.percepcionIibb,
        desactivado = this.desactivado,
        tipoIvaId = this.tipoIva?.id,
        provinciaId = this.provincia?.id
    )
}

fun List<Cliente?>.toClienteEntityList(): List<ClienteEntity> {
    return this.mapNotNull { it?.toEntity() }
}

// --- Producto Mapper ---
fun Producto.toEntity(): ProductoEntity {
    return ProductoEntity(
        id = this.id,
        codigo = this.codigo,
        descripcion = this.descripcion,
        descripcionAmpliada = this.descripcionAmpliada,
        stock = this.stock,
        stockMinimo = this.stockMinimo,
        stockPedido = this.stockPedido,
        codigoBarra = this.codigoBarra,
        articuloActivado = this.articuloActivado,
        productoBalanza = this.productoBalanza,
        precio1 = this.precio1,
        precio2 = this.precio2,
        precio3 = this.precio3,
        precio4 = this.precio4,
        monedaId = this.moneda?.id,
        tasaIvaId = this.tasaIva?.id,
        incluyeIva = this.incluyeIva,
        impuestoInterno = this.impuestoInterno,
        tipoImpuestoInterno = this.tipoImpuestoInterno,
        precio1ImpuestoInterno = this.precio1ImpuestoInterno,
        precio2ImpuestoInterno = this.precio2ImpuestoInterno,
        precio3ImpuestoInterno = this.precio3ImpuestoInterno,
        precioCosto = this.precioCosto,
        fraccionado = this.fraccionado,
        rg5329_23 = this.rg5329_23,
        activo = this.activo,
        textoPanel = this.textoPanel,
        iibb = this.iibb,
        codigoBarra2 = this.codigoBarra2,
        oferta = this.oferta,
        margenGanancia = this.margenGanancia,
        favorito = this.favorito,
        familiaId = this.familia?.id,
        agrupacionId = this.agrupacion?.id,
        proveedorId = this.proveedor?.id,
        tipoId = this.tipo?.id,
        unidadId = this.unidad?.id
        // Nota: Los campos como stockActual (lista) y combinaciones (lista) en Producto.kt
        // necesitarían un manejo más complejo si quieres persistirlos tal cual
        // (ej. TypeConverters para JSON o tablas adicionales).
        // Por simplicidad, ProductoEntity no los incluye de esa forma directa.
    )
}

fun List<Producto?>.toProductoEntityList(): List<ProductoEntity> {
    return this.mapNotNull { it?.toEntity() }
}

// --- Mappers para otras entidades (Ejemplo: Agrupacion) ---
fun Agrupacion.toEntity(): AgrupacionEntity {
    return AgrupacionEntity(
        id = this.id,
        numero = this.numero,
        nombre = this.nombre,
        color = this.color,
        icono = this.icono
    )
}

fun List<Agrupacion?>.toAgrupacionEntityList(): List<AgrupacionEntity> {
    return this.mapNotNull { it?.toEntity() }
}

