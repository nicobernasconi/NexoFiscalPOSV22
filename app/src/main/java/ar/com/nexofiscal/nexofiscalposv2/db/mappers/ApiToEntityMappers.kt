package ar.com.nexofiscal.nexofiscalposv2.db.mappers

import ar.com.nexofiscal.nexofiscalposv2.db.entity.*
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ComprobanteConDetalle
import ar.com.nexofiscal.nexofiscalposv2.models.*
import ar.com.nexofiscal.nexofiscalposv2.screens.Pago
import com.google.gson.Gson
import java.util.Locale

// MAPPERS DE ENTIDAD A MODELO DE DOMINIO (API)
// Nota: Se han corregido los mappers para que solo asignen los campos que realmente existen en el modelo de dominio.

fun List<AgrupacionEntity>.toAgrupacionDomainModelList(): List<Agrupacion> {
    return this.map { it.toDomainModel() }
}

fun List<CategoriaEntity>.toCategoriaDomainModelList(): List<Categoria> {
    return this.map { it.toDomainModel() }
}

fun CierreCajaEntity.toDomainModel(): CierreCaja {
    val domain = CierreCaja()
    domain.id = this.serverId ?: this.id
    domain.fecha = this.fecha
    domain.totalVentas = this.totalVentas
    domain.totalGastos = this.totalGastos
    domain.efectivoInicial = this.efectivoInicial
    domain.efectivoFinal = this.efectivoFinal
    domain.tipoCajaId = this.tipoCajaId
    this.usuarioId?.let { domain.usuario = Usuario().apply { id = it } }
    return domain
}

fun List<CierreCajaEntity>.toCierreCajaDomainModelList(): List<CierreCaja> {
    return this.map { it.toDomainModel() }
}

fun ClienteEntity.toDomainModel(): Cliente {
    val domain = Cliente()
    domain.id = this.serverId ?: this.id
    domain.localId = this.id
    domain.nroCliente = this.nroCliente
    domain.nombre = this.nombre
    domain.cuit = this.cuit
    this.tipoDocumentoId?.let { domain.tipoDocumento = TipoDocumento().apply { id = it } }
    domain.numeroDocumento = this.numeroDocumento
    domain.direccionComercial = this.direccionComercial
    domain.direccionEntrega = this.direccionEntrega
    this.localidadId?.let { domain.localidad = Localidad().apply { id = it } }
    domain.telefono = this.telefono
    domain.celular = this.celular
    domain.email = this.email
    domain.contacto = this.contacto
    domain.telefonoContacto = this.telefonoContacto
    this.categoriaId?.let { domain.categoria = Categoria().apply { id = it } }
    this.vendedoresId?.let { domain.vendedores = Vendedor().apply { id = it } }
    domain.porcentajeDescuento = this.porcentajeDescuento
    domain.limiteCredito = this.limiteCredito
    domain.saldoInicial = this.saldoInicial
    domain.saldoActual = this.saldoActual
    domain.fechaUltimaCompra = this.fechaUltimaCompra
    domain.fechaUltimoPago = this.fechaUltimoPago
    domain.percepcionIibb = this.percepcionIibb
    domain.desactivado = this.desactivado
    this.tipoIvaId?.let {
        domain.tipoIva = TipoIVA().apply { id = it }
        domain.tipoIvaId = it
    }
    this.provinciaId?.let { domain.provincia = Provincia().apply { id = it } }
    return domain
}

fun List<ClienteEntity>.toClienteDomainModelList(): List<Cliente> {
    return this.map { it.toDomainModel() }
}

fun CombinacionEntity.toDomainModel(): Combinacion {
    return Combinacion(
        productoPrincipalId = this.productoPrincipalId,
        subproductoId = this.subproductoId,
        cantidad = this.cantidad,
        empresaId = this.empresaId
    )
}

fun List<CombinacionEntity>.toCombinacionDomainModelList(): List<Combinacion> {
    return this.map { it.toDomainModel() }
}

fun ComprobanteEntity.toDomainModel(): Comprobante {
    return Comprobante(
        localId = this.id,
        id = this.serverId ?: 0,
        serverId = this.serverId,
        numero = this.numero,
        cuotas = this.cuotas,
        clienteId = this.clienteId,
        remito = this.remito,
        persona = this.persona,
        provinciaId = this.provinciaId,
        fecha = this.fecha,
        fechaBaja = this.fechaBaja,
        motivoBaja = this.motivoBaja,
        hora = this.hora,
        fechaProceso = this.fechaProceso,
        letra = this.letra,
        numeroFactura = this.numeroFactura,
        prefijoFactura = this.prefijoFactura,
        operacionNegocioId = this.operacionNegocioId,
        retencionIva = this.retencionIva,
        retencionIibb = this.retencionIibb,
        retencionGanancias = this.retencionGanancias,
        porcentajeGanancias = this.porcentajeGanancias,
        porcentajeIibb = this.porcentajeIibb,
        porcentajeIva = this.porcentajeIva,
        noGravado = this.noGravado,
        importeIva = this.importeIva,
        total = this.total,
        totalPagado = this.totalPagado,
        condicionVentaId = this.condicionVentaId,
        descripcionFlete = this.descripcionFlete,
        vendedorId = this.vendedorId,
        recibo = this.recibo,
        observaciones1 = this.observaciones1,
        observaciones2 = this.observaciones2,
        observaciones3 = this.observaciones3,
        observaciones4 = this.observaciones4,
        descuento = this.descuento,
        descuento1 = this.descuento1,
        descuento2 = this.descuento2,
        descuento3 = this.descuento3,
        descuento4 = this.descuento4,
        iva2 = this.iva2,
        impresa = this.impresa,
        cancelado = this.cancelado,
        nombreCliente = this.nombreCliente,
        direccionCliente = this.direccionCliente,
        localidadCliente = this.localidadCliente,
        garantia = this.garantia,
        concepto = this.concepto,
        notas = this.notas,
        lineaPagoUltima = this.lineaPagoUltima,
        relacionTk = this.relacionTk,
        totalIibb = this.totalIibb,
        importeIibb = this.importeIibb,
        provinciaCategoriaIibbId = this.provinciaCategoriaIibbId,
        importeRetenciones = this.importeRetenciones,
        provinciaIvaProveedorId = this.provinciaIvaProveedorId,
        gananciasProveedorId = this.gananciasProveedorId,
        importeGanancias = this.importeGanancias,
        numeroIibb = this.numeroIibb,
        numeroGanancias = this.numeroGanancias,
        gananciasProveedor = this.gananciasProveedor,
        cae = this.cae,
        fechaVencimiento = this.fechaVencimiento,
        remitoCliente = this.remitoCliente,
        textoDolares = this.textoDolares,
        comprobanteFinal = this.comprobanteFinal,
        numeroGuia1 = this.numeroGuia1,
        numeroGuia2 = this.numeroGuia2,
        numeroGuia3 = this.numeroGuia3,
        tipoAlicuota1 = this.tipoAlicuota1,
        tipoAlicuota2 = this.tipoAlicuota2,
        tipoAlicuota3 = this.tipoAlicuota3,
        importeIva105 = this.importeIva105,
        importeIva21 = this.importeIva21,
        importeIva0 = this.importeIva0,
        noGravadoIva105 = this.noGravadoIva105,
        noGravadoIva21 = this.noGravadoIva21,
        noGravadoIva0 = this.noGravadoIva0,
        direccionEntrega = this.direccionEntrega,
        fechaEntrega = this.fechaEntrega,
        horaEntrega = this.horaEntrega,
        empresaId = this.empresaId,
        puntoVenta = this.puntoVenta,
        tipoFactura = this.tipoFactura,
        tipoDocumento = this.tipoDocumento,
        numeroDeDocumento = this.numeroDeDocumento,
        qr = this.qr,
        comprobanteIdBaja = this.comprobanteIdBaja,
        sucursalId = this.sucursalId,
        descuentoTotal = this.descuentoTotal,
        incrementoTotal = this.incrementoTotal,
        cliente = null,
        vendedor = null,
        provincia = null,
        tipoComprobante = null,
        formas_de_pago = emptyList(),
        promociones = null,
        tipoComprobanteId = this.tipoComprobanteId,




    )
}

fun List<ComprobanteEntity>.toComprobanteDomainModelList(): List<Comprobante> {
    return this.map { it.toDomainModel() }
}

fun FamiliaEntity.toDomainModel(): Familia {
    val domain = Familia()
    domain.localId = this.id // <-- ¡CORRECCIÓN CLAVE! Asigna el ID local.
    domain.id = this.serverId ?: 0
    domain.numero = this.numero
    domain.nombre = this.nombre
    return domain
}



fun List<FamiliaEntity>.toFamiliaDomainModelList(): List<Familia> {
    return this.map { it.toDomainModel() }
}

fun List<FormaPagoEntity>.toFormaPagoDomainModelList(): List<FormaPago> {
    return this.map { it.toDomainModel() }
}

fun LocalidadEntity.toDomainModel(): Localidad {
    val domain = Localidad()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    domain.codigoPostal = this.codigoPostal
    this.provinciaId?.let { domain.provincia = Provincia().apply { id = it } }
    return domain
}

fun List<LocalidadEntity>.toLocalidadDomainModelList(): List<Localidad> {
    return this.map { it.toDomainModel() }
}


fun List<MonedaEntity>.toMonedaDomainModelList(): List<Moneda> {
    return this.map { it.toDomainModel() }
}

fun PaisEntity.toDomainModel(): Pais {
    val domain = Pais()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    return domain
}

fun List<PaisEntity>.toPaisDomainModelList(): List<Pais> {
    return this.map { it.toDomainModel() }
}

fun ProductoEntity.toDomainModel(): Producto {
    val domain = Producto()
    domain.localId = this.id
    domain.id = this.serverId ?: this.id
    domain.codigo = this.codigo
    domain.descripcion = this.descripcion
    domain.descripcionAmpliada = this.descripcionAmpliada
    domain.stock = this.stock
    domain.stockMinimo = this.stockMinimo
    domain.stockPedido = this.stockPedido
    domain.codigoBarra = this.codigoBarra
    domain.articuloActivado = this.articuloActivado
    domain.productoBalanza = this.productoBalanza
    domain.precio1 = this.precio1
    domain.precio2 = this.precio2
    domain.precio3 = this.precio3
    domain.precio4 = this.precio4
    this.monedaId?.let { domain.moneda = Moneda().apply { id = it } }
    this.tasaIvaId?.let { domain.tasaIva = TasaIva().apply { id = it } }
    domain.incluyeIva = this.incluyeIva
    domain.impuestoInterno = this.impuestoInterno
    domain.tipoImpuestoInterno = this.tipoImpuestoInterno
    domain.precio1ImpuestoInterno = this.precio1ImpuestoInterno
    domain.precio2ImpuestoInterno = this.precio2ImpuestoInterno
    domain.precio3ImpuestoInterno = this.precio3ImpuestoInterno
    domain.precioCosto = this.precioCosto
    domain.fraccionado = this.fraccionado
    domain.rg5329_23 = this.rg5329_23
    domain.activo = this.activo
    domain.textoPanel = this.textoPanel
    domain.iibb = this.iibb
    domain.codigoBarra2 = this.codigoBarra2
    domain.oferta = this.oferta
    domain.margenGanancia = this.margenGanancia
    domain.favorito = this.favorito
    this.familiaId?.let { domain.familia = Familia().apply { id = it } }
    this.agrupacionId?.let { domain.agrupacion = Agrupacion().apply { id = it } }
    this.proveedorId?.let { domain.proveedor = Proveedor().apply { id = it } }
    this.tipoId?.let { domain.tipo = Tipo().apply { id = it } }
    this.unidadId?.let { domain.unidad = Unidad().apply { id = it } }
    return domain
}

fun ProductoConDetalles.toDomainModel(): Producto {
    // Esta función ya usa ProductoEntity.toDomainModel(), así que heredará la corrección.
    val domainProducto = this.producto.toDomainModel()
    domainProducto.moneda = this.moneda?.toDomainModel()
    domainProducto.tasaIva = this.tasaIva?.toDomainModel()
    domainProducto.familia = this.familia?.toDomainModel()
    domainProducto.agrupacion = this.agrupacion?.toDomainModel()
    domainProducto.proveedor = this.proveedor?.toDomainModel()
    domainProducto.tipo = this.tipo?.toDomainModel()
    domainProducto.unidad = this.unidad?.toDomainModel()
    return domainProducto
}

fun List<ProductoEntity>.toProductoDomainModelList(): List<Producto> {
    return this.map { it.toDomainModel() }
}



fun List<PromocionEntity>.toPromocionDomainModelList(): List<Promocion> {
    return this.map { it.toDomainModel() }
}

fun ProveedorEntity.toDomainModel(): Proveedor {
    val domain = Proveedor()
    domain.localId = this.id // <-- ¡CORRECCIÓN CLAVE! Asigna el ID local.
    domain.id = this.serverId ?: 0
    domain.razonSocial = this.razonSocial
    domain.direccion = this.direccion
    domain.telefono = this.telefono
    domain.email = this.email
    domain.cuit = this.cuit
    domain.fechaUltimaCompra = this.fechaUltimaCompra
    domain.fechaUltimoPago = this.fechaUltimoPago
    domain.saldoActual = this.saldoActual
    // Las entidades relacionadas se cargan en el ViewModel
    return domain
}

// Mapper de Modelo de Dominio (UI) a Entidad de DB
fun Proveedor.toEntity(): ProveedorEntity {
    return ProveedorEntity(
        id = this.localId, // <-- ¡CORRECCIÓN CLAVE! Usa el ID local guardado.
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED, // El ViewModel lo ajustará.
        razonSocial = this.razonSocial,
        direccion = this.direccion,
        localidadId = this.localidad?.id,
        telefono = this.telefono,
        email = this.email,
        tipoIvaId = this.tipoIva?.id,
        cuit = this.cuit,
        categoriaId = this.categoria?.id,
        subcategoriaId = this.subcategoria?.id,
        fechaUltimaCompra = this.fechaUltimaCompra,
        fechaUltimoPago = this.fechaUltimoPago,
        saldoActual = this.saldoActual
    )
}

fun List<ProveedorEntity>.toProveedorDomainModelList(): List<Proveedor> {
    return this.map { it.toDomainModel() }
}

fun ProvinciaEntity.toDomainModel(): Provincia {
    val domain = Provincia()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    this.paisId?.let { domain.pais = Pais().apply { id = it } }
    return domain
}

fun List<ProvinciaEntity>.toProvinciaDomainModelList(): List<Provincia> {
    return this.map { it.toDomainModel() }
}

fun RenglonComprobanteEntity.toDomainModel(): RenglonComprobante {
    val gson = Gson()
    return gson.fromJson(this.data, RenglonComprobante::class.java)
}

fun List<RenglonComprobanteEntity>.toRenglonComprobanteDomainModelList(): List<RenglonComprobante> {
    return this.map { it.toDomainModel() }
}

fun RolEntity.toDomainModel(): Rol {
    val domain = Rol()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    domain.descripcion = this.descripcion
    return domain
}

fun List<RolEntity>.toRolDomainModelList(): List<Rol> {
    return this.map { it.toDomainModel() }
}

fun StockProductoEntity.toDomainModel(): StockProducto {
    val domain = StockProducto()
    domain.id = this.serverId ?: this.id
    domain.codigo = this.codigo
    domain.stockInicial = this.stockInicial
    domain.controlaStock = this.controlaStock
    domain.puntoPedido = this.puntoPedido
    domain.largo = this.largo
    domain.alto = this.alto
    domain.ancho = this.ancho
    domain.peso = this.peso
    domain.unidadId = this.unidadId
    domain.ubicacionId = this.ubicacionId
    domain.proveedoresId = this.proveedoresId
    domain.productoId = this.productoId
    domain.empresaId = this.empresaId
    domain.stockActual = this.stockActual
    domain.sucursalId = this.sucursalId
    return domain
}

fun List<StockProductoEntity>.toStockProductoDomainModelList(): List<StockProducto> {
    return this.map { it.toDomainModel() }
}

fun SubcategoriaEntity.toDomainModel(): Subcategoria {
    val domain = Subcategoria()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    domain.seImprime = this.seImprime
    return domain
}

fun List<SubcategoriaEntity>.toSubcategoriaDomainModelList(): List<Subcategoria> {
    return this.map { it.toDomainModel() }
}

fun SucursalEntity.toDomainModel(): Sucursal {
    val domain = Sucursal()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    domain.direccion = this.direccion
    // CAMBIO: Faltaban campos que no existen en la entidad.
    // domain.empresaId = this.empresaId 
    // domain.telefono = this.telefono
    // domain.email = this.email
    // domain.contactoNombre = this.contactoNombre
    // ...
    return domain
}

fun List<SucursalEntity>.toSucursalDomainModelList(): List<Sucursal> {
    return this.map { it.toDomainModel() }
}

fun TasaIvaEntity.toDomainModel(): TasaIva {
    val domain = TasaIva()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    domain.tasa = this.tasa
    return domain
}

fun List<TasaIvaEntity>.toTasaIvaDomainModelList(): List<TasaIva> {
    return this.map { it.toDomainModel() }
}

fun TipoComprobanteEntity.toDomainModel(): TipoComprobante {
    val domain = TipoComprobante()
    domain.id = this.serverId ?: this.id
    // CAMBIO: El campo 'numero' no existe en TipoComprobanteEntity
    // domain.numero = this.numero 
    domain.nombre = this.nombre
    return domain
}

fun List<TipoComprobanteEntity>.toTipoComprobanteDomainModelList(): List<TipoComprobante> {
    return this.map { it.toDomainModel() }
}

fun List<TipoDocumentoEntity>.toTipoDocumentoDomainModelList(): List<TipoDocumento> {
    return this.map { it.toDomainModel() }
}

fun TipoEntity.toDomainModel(): Tipo {
    val domain = Tipo()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    // CAMBIO: El campo 'numero' no existe en TipoEntity
    // domain.numero = this.numero
    return domain
}

fun List<TipoEntity>.toTipoDomainModelList(): List<Tipo> {
    return this.map { it.toDomainModel() }
}

fun TipoFormaPagoEntity.toDomainModel(): TipoFormaPago {
    val domain = TipoFormaPago()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    return domain
}

fun FormaPagoConDetalles.toDomainModel(): FormaPago {
    // 1. Convierte la parte principal (FormaPagoEntity) al modelo de dominio.
    val domainModel = this.formaPago.toDomainModel()

    // 2. Convierte la parte relacionada (TipoFormaPagoEntity) y la asigna.
    domainModel.tipoFormaPago = this.tipoFormaPago?.toDomainModel()

    return domainModel
}


fun List<TipoFormaPagoEntity>.toTipoFormaPagoDomainModelList(): List<TipoFormaPago> {
    return this.map { it.toDomainModel() }
}

fun TipoIvaEntity.toDomainModel(): TipoIVA {
    val domain = TipoIVA()
    domain.localId = this.id // <-- ¡CORRECCIÓN CLAVE! Asigna el ID local.
    domain.id = this.serverId ?: 0
    domain.nombre = this.nombre
    // Los campos 'letraFactura' y 'porcentaje' no están en la entidad, se mantendrán null
    return domain
}

// Mapper de Modelo de Dominio (UI) a Entidad de DB
fun TipoIVA.toEntity(): TipoIvaEntity {
    return TipoIvaEntity(
        id = this.localId, // <-- ¡CORRECCIÓN CLAVE! Usa el ID local guardado.
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED, // El ViewModel lo ajustará.
        nombre = this.nombre,
        descripcion = null // La entidad tiene un campo 'descripcion' que el modelo no, se asigna null.
    )
}

fun List<TipoIvaEntity>.toTipoIvaDomainModelList(): List<TipoIVA> {
    return this.map { it.toDomainModel() }
}

fun List<UnidadEntity>.toUnidadDomainModelList(): List<Unidad> {
    return this.map { it.toDomainModel() }
}

fun UsuarioEntity.toDomainModel(): Usuario {
    val domain = Usuario()
    domain.id = this.serverId ?: this.id
    domain.nombreUsuario = this.nombreUsuario
    domain.nombreCompleto = this.nombreCompleto
    // CAMBIO: Faltaban campos que no existen en la entidad.
    // domain.password = this.password 
    // domain.activo = this.activo
    // domain.empresaId = this.empresaId
    this.rolId?.let { domain.rol = Rol().apply { id = it } }
    this.sucursalId?.let { domain.sucursal = Sucursal().apply { id = it } }
    this.vendedorId?.let { domain.vendedor = Vendedor().apply { id = it } }
    return domain
}

fun List<UsuarioEntity>.toUsuarioDomainModelList(): List<Usuario> {
    return this.map { it.toDomainModel() }
}

fun VendedorEntity.toDomainModel(): Vendedor {
    val domain = Vendedor()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    domain.porcentajeComision = this.porcentajeComision
    // CAMBIO: Faltaban campos que no existen en la entidad.
    // domain.direccion = this.direccion
    // domain.telefono = this.telefono
    // domain.fechaIngreso = this.fechaIngreso
    return domain
}

fun List<VendedorEntity>.toVendedorDomainModelList(): List<Vendedor> {
    return this.map { it.toDomainModel() }
}


// MAPPERS DE MODELO DE DOMINIO (API) A ENTIDAD
// Nota: Se han corregido todos los mappers para asignar `serverId`, `id` local y `syncStatus`.
fun AgrupacionEntity.toDomainModel(): Agrupacion {
    val domain = Agrupacion()
    domain.localId = this.id // <-- ¡CORRECCIÓN CLAVE! Asigna el ID local.
    domain.id = this.serverId ?: 0
    domain.numero = this.numero
    domain.nombre = this.nombre
    domain.color = this.color
    domain.icono = this.icono
    return domain
}

// Mapper de Modelo de Dominio (UI) a Entidad de DB
fun Agrupacion.toEntity(): AgrupacionEntity {
    return AgrupacionEntity(
        id = this.localId, // <-- ¡CORRECCIÓN CLAVE! Usa el ID local guardado.
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED, // El ViewModel lo ajustará.
        numero = this.numero,
        nombre = this.nombre,
        color = this.color,
        icono = this.icono
    )
}
fun List<Agrupacion?>.toAgrupacionEntityList(): List<AgrupacionEntity> = this.mapNotNull { it?.toEntity() }

fun CategoriaEntity.toDomainModel(): Categoria {
    val domain = Categoria()
    domain.localId = this.id // <-- ¡CORRECCIÓN CLAVE! Asigna el ID local.
    domain.id = this.serverId
    domain.nombre = this.nombre
    domain.seImprime = this.seImprime
    return domain
}

// Mapper de Modelo de Dominio (UI) a Entidad de DB
fun Categoria.toEntity(): CategoriaEntity {
    return CategoriaEntity(
        id = this.localId, // <-- ¡CORRECCIÓN CLAVE! Usa el ID local guardado.
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED, // El ViewModel lo ajustará.
        nombre = this.nombre,
        seImprime = this.seImprime
    )
}
fun List<Categoria?>.toCategoriaEntityList(): List<CategoriaEntity> = this.mapNotNull { it?.toEntity() }

fun CierreCaja.toEntity(): CierreCajaEntity {
    return CierreCajaEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        fecha = this.fecha,
        totalVentas = this.totalVentas,
        totalGastos = this.totalGastos,
        efectivoInicial = this.efectivoInicial,
        efectivoFinal = this.efectivoFinal,
        tipoCajaId = this.tipoCajaId,
        usuarioId = this.usuario?.id
    )
}
fun List<CierreCaja?>.toCierreCajaEntityList(): List<CierreCajaEntity> = this.mapNotNull { it?.toEntity() }

fun Cliente.toEntity(): ClienteEntity {
    return ClienteEntity(
        id = this.localId,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
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
fun List<Cliente?>.toClienteEntityList(): List<ClienteEntity> = this.mapNotNull { it?.toEntity() }

fun Combinacion.toEntity(): CombinacionEntity {
    return CombinacionEntity(
        uid = 0,
        syncStatus = SyncStatus.SYNCED,
        productoPrincipalId = this.productoPrincipalId,
        subproductoId = this.subproductoId,
        cantidad = this.cantidad,
        empresaId = this.empresaId
    )
}
fun List<Combinacion?>.toCombinacionEntityList(): List<CombinacionEntity> = this.mapNotNull { it?.toEntity() }

fun Comprobante.toEntity(): ComprobanteEntity {
    return ComprobanteEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        numero = this.numero,
        cuotas = this.cuotas,
        clienteId = this.clienteId,
        remito = this.remito,
        persona = this.persona,
        provinciaId = this.provinciaId,
        fecha = this.fecha,
        fechaBaja = this.fechaBaja,
        motivoBaja = this.motivoBaja,
        hora = this.hora,
        fechaProceso = this.fechaProceso,
        letra = this.letra,
        numeroFactura = this.numeroFactura,
        prefijoFactura = this.prefijoFactura,
        operacionNegocioId = this.operacionNegocioId,
        retencionIva = this.retencionIva,
        retencionIibb = this.retencionIibb,
        retencionGanancias = this.retencionGanancias,
        porcentajeGanancias = this.porcentajeGanancias,
        porcentajeIibb = this.porcentajeIibb,
        porcentajeIva = this.porcentajeIva,
        noGravado = this.noGravado,
        importeIva = this.importeIva,
        total = this.total,
        totalPagado = this.totalPagado,
        condicionVentaId = this.condicionVentaId,
        descripcionFlete = this.descripcionFlete,
        vendedorId = this.vendedorId,
        recibo = this.recibo,
        observaciones1 = this.observaciones1,
        observaciones2 = this.observaciones2,
        observaciones3 = this.observaciones3,
        observaciones4 = this.observaciones4,
        descuento = this.descuento,
        descuento1 = this.descuento1,
        descuento2 = this.descuento2,
        descuento3 = this.descuento3,
        descuento4 = this.descuento4,
        iva2 = this.iva2,
        impresa = this.impresa,
        cancelado = this.cancelado,
        nombreCliente = this.nombreCliente,
        direccionCliente = this.direccionCliente,
        localidadCliente = this.localidadCliente,
        garantia = this.garantia,
        concepto = this.concepto,
        notas = this.notas,
        lineaPagoUltima = this.lineaPagoUltima,
        relacionTk = this.relacionTk,
        totalIibb = this.totalIibb,
        importeIibb = this.importeIibb,
        provinciaCategoriaIibbId = this.provinciaCategoriaIibbId,
        importeRetenciones = this.importeRetenciones,
        provinciaIvaProveedorId = this.provinciaIvaProveedorId,
        gananciasProveedorId = this.gananciasProveedorId,
        importeGanancias = this.importeGanancias,
        numeroIibb = this.numeroIibb,
        numeroGanancias = this.numeroGanancias,
        gananciasProveedor = this.gananciasProveedor,
        cae = this.cae,
        fechaVencimiento = this.fechaVencimiento,
        remitoCliente = this.remitoCliente,
        textoDolares = this.textoDolares,
        comprobanteFinal = this.comprobanteFinal,
        numeroGuia1 = this.numeroGuia1,
        numeroGuia2 = this.numeroGuia2,
        numeroGuia3 = this.numeroGuia3,
        tipoAlicuota1 = this.tipoAlicuota1,
        tipoAlicuota2 = this.tipoAlicuota2,
        tipoAlicuota3 = this.tipoAlicuota3,
        importeIva105 = this.importeIva105,
        importeIva21 = this.importeIva21,
        importeIva0 = this.importeIva0,
        noGravadoIva105 = this.noGravadoIva105,
        noGravadoIva21 = this.noGravadoIva21,
        noGravadoIva0 = this.noGravadoIva0,
        direccionEntrega = this.direccionEntrega,
        fechaEntrega = this.fechaEntrega,
        horaEntrega = this.horaEntrega,
        empresaId = this.empresaId,
        puntoVenta = this.puntoVenta,
        tipoFactura = this.tipoFactura,
        tipoDocumento = this.tipoDocumento,
        numeroDeDocumento = this.numeroDeDocumento,
        qr = this.qr,
        comprobanteIdBaja = this.comprobanteIdBaja,
        sucursalId = this.sucursalId,
        descuentoTotal = this.descuentoTotal,
        incrementoTotal = this.incrementoTotal,
        tipoComprobanteId = this.tipoComprobante?.id,
    )
}
fun List<Comprobante?>.toComprobanteEntityList(): List<ComprobanteEntity> = this.mapNotNull { it?.toEntity() }

fun Familia.toEntity(): FamiliaEntity {
    return FamiliaEntity(
        id = this.localId,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        numero = this.numero,
        nombre = this.nombre
    )
}
fun List<Familia?>.toFamiliaEntityList(): List<FamiliaEntity> = this.mapNotNull { it?.toEntity() }

fun FormaPagoEntity.toDomainModel(): FormaPago {
    val domain = FormaPago()
    domain.localId = this.id // <-- ¡CORRECCIÓN CLAVE! Asigna el ID local.
    domain.id = this.serverId ?: 0
    domain.nombre = this.nombre
    domain.porcentaje = this.porcentaje
    // La lógica para cargar `tipoFormaPago` se mantiene en el ViewModel.
    return domain
}

// Mapper de Modelo de Dominio a Entidad
fun FormaPago.toEntity(): FormaPagoEntity {
    return FormaPagoEntity(
        id = this.localId, // <-- ¡CORRECCIÓN CLAVE! Usa el ID local guardado.
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED, // El ViewModel lo ajustará
        nombre = this.nombre,
        porcentaje = this.porcentaje,
        tipoFormaPagoId = this.tipoFormaPago?.id
    )
}
fun List<FormaPago?>.toFormaPagoEntityList(): List<FormaPagoEntity> = this.mapNotNull { it?.toEntity() }

fun Localidad.toEntity(): LocalidadEntity {
    return LocalidadEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre,
        codigoPostal = this.codigoPostal,
        provinciaId = this.provincia?.id
    )
}
fun List<Localidad?>.toLocalidadEntityList(): List<LocalidadEntity> = this.mapNotNull { it?.toEntity() }

fun MonedaEntity.toDomainModel(): Moneda {
    val domain = Moneda()
    domain.localId = this.id // <-- ¡CORRECCIÓN CLAVE! Asigna el ID local.
    domain.id = this.serverId ?: 0
    domain.simbolo = this.simbolo
    domain.nombre = this.nombre
    domain.cotizacion = this.cotizacion
    return domain
}

// Mapper de Modelo de Dominio a Entidad
fun Moneda.toEntity(): MonedaEntity {
    return MonedaEntity(
        id = this.localId, // <-- ¡CORRECCIÓN CLAVE! Usa el ID local guardado.
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        simbolo = this.simbolo,
        nombre = this.nombre,
        cotizacion = this.cotizacion
    )
}
fun List<Moneda?>.toMonedaEntityList(): List<MonedaEntity> = this.mapNotNull { it?.toEntity() }

fun Pais.toEntity(): PaisEntity {
    return PaisEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre
    )
}
fun List<Pais?>.toPaisEntityList(): List<PaisEntity> = this.mapNotNull { it?.toEntity() }

fun Producto.toEntity(): ProductoEntity {
    return ProductoEntity(
        id = this.localId,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
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
    )
}
fun List<Producto?>.toProductoEntityList(): List<ProductoEntity> = this.mapNotNull { it?.toEntity() }

fun PromocionEntity.toDomainModel(): Promocion {
    val domain = Promocion()
    domain.localId = this.id // <-- ¡CORRECCIÓN CLAVE! Asigna el ID local.
    domain.id = this.serverId ?: 0
    domain.nombre = this.nombre
    domain.descripcion = this.descripcion
    domain.porcentaje = this.porcentaje
    return domain
}

// Mapper de Modelo de Dominio (UI) a Entidad de DB
fun Promocion.toEntity(): PromocionEntity {
    return PromocionEntity(
        id = this.localId, // <-- ¡CORRECCIÓN CLAVE! Usa el ID local guardado.
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED, // El ViewModel lo ajustará.
        nombre = this.nombre,
        descripcion = this.descripcion,
        porcentaje = this.porcentaje
    )
}
fun List<Promocion?>.toPromocionEntityList(): List<PromocionEntity> = this.mapNotNull { it?.toEntity() }

fun List<Proveedor?>.toProveedorEntityList(): List<ProveedorEntity> = this.mapNotNull { it?.toEntity() }

fun Provincia.toEntity(): ProvinciaEntity {
    return ProvinciaEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre,
        paisId = this.pais?.id
    )
}
fun List<Provincia?>.toProvinciaEntityList(): List<ProvinciaEntity> = this.mapNotNull { it?.toEntity() }




fun Rol.toEntity(): RolEntity {
    return RolEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre,
        descripcion = this.descripcion
    )
}
fun List<Rol?>.toRolEntityList(): List<RolEntity> = this.mapNotNull { it?.toEntity() }

fun StockProducto.toEntity(): StockProductoEntity {
    return StockProductoEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        codigo = this.codigo,
        stockInicial = this.stockInicial,
        controlaStock = this.controlaStock,
        puntoPedido = this.puntoPedido,
        largo = this.largo,
        alto = this.alto,
        ancho = this.ancho,
        peso = this.peso,
        unidadId = this.unidadId,
        ubicacionId = this.ubicacionId,
        proveedoresId = this.proveedoresId,
        productoId = this.productoId,
        empresaId = this.empresaId,
        stockActual = this.stockActual,
        sucursalId = this.sucursalId
    )
}
fun List<StockProducto?>.toStockProductoEntityList(): List<StockProductoEntity> = this.mapNotNull { it?.toEntity() }

fun Subcategoria.toEntity(): SubcategoriaEntity {
    return SubcategoriaEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre,
        seImprime = this.seImprime
    )
}
fun List<Subcategoria?>.toSubcategoriaEntityList(): List<SubcategoriaEntity> = this.mapNotNull { it?.toEntity() }

fun Sucursal.toEntity(): SucursalEntity {
    return SucursalEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre,
        direccion = this.direccion
        // CAMBIO: Los campos que no existen en la entidad se eliminan del mapper.
    )
}
fun List<Sucursal?>.toSucursalEntityList(): List<SucursalEntity> = this.mapNotNull { it?.toEntity() }

fun TasaIva.toEntity(): TasaIvaEntity {
    return TasaIvaEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre,
        tasa = this.tasa
    )
}
fun List<TasaIva?>.toTasaIvaEntityList(): List<TasaIvaEntity> = this.mapNotNull { it?.toEntity() }

fun Tipo.toEntity(): TipoEntity {
    return TipoEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre
    )
}
fun List<Tipo?>.toTipoEntityList(): List<TipoEntity> = this.mapNotNull { it?.toEntity() }

fun TipoComprobante.toEntity(): TipoComprobanteEntity {
    return TipoComprobanteEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre
    )
}
fun List<TipoComprobante?>.toTipoComprobanteEntityList(): List<TipoComprobanteEntity> = this.mapNotNull { it?.toEntity() }

fun TipoDocumentoEntity.toDomainModel(): TipoDocumento {
    val domain = TipoDocumento()
    domain.localId = this.id // <-- ¡CORRECCIÓN CLAVE! Asigna el ID local.
    domain.id = this.serverId ?: 0
    domain.nombre = this.nombre
    return domain
}

// Mapper de Modelo de Dominio (UI) a Entidad de DB
fun TipoDocumento.toEntity(): TipoDocumentoEntity {
    return TipoDocumentoEntity(
        id = this.localId, // <-- ¡CORRECCIÓN CLAVE! Usa el ID local guardado.
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED, // El ViewModel lo ajustará.
        nombre = this.nombre,
        abrev = null, // Asumimos que estos campos no se gestionan desde la app
        descripcion = null
    )
}
fun List<TipoDocumento?>.toTipoDocumentoEntityList(): List<TipoDocumentoEntity> = this.mapNotNull { it?.toEntity() }

fun TipoFormaPago.toEntity(): TipoFormaPagoEntity {
    return TipoFormaPagoEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre
    )
}
fun List<TipoFormaPago?>.toTipoFormaPagoEntityList(): List<TipoFormaPagoEntity> = this.mapNotNull { it?.toEntity() }

fun List<TipoIVA?>.toTipoIvaEntityList(): List<TipoIvaEntity> = this.mapNotNull { it?.toEntity() }

fun UnidadEntity.toDomainModel(): Unidad {
    val domain = Unidad()
    domain.localId = this.id // <-- ¡CORRECCIÓN CLAVE! Asigna el ID local.
    domain.id = this.serverId ?: 0
    domain.nombre = this.nombre
    domain.simbolo = this.simbolo
    // El campo 'simbolo' no existe en UnidadEntity, por lo que se mantiene la lógica actual.
    return domain
}

// Mapper de Modelo de Dominio (UI) a Entidad de DB
fun Unidad.toEntity(): UnidadEntity {
    return UnidadEntity(
        id = this.localId, // <-- ¡CORRECCIÓN CLAVE! Usa el ID local guardado.
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED, // El ViewModel lo ajustará.
        nombre = this.nombre,
        simbolo = this.simbolo ?: "", // Asignamos un valor por defecto si es null.
        // El campo 'simbolo' no existe en UnidadEntity.
    )
}
fun List<Unidad?>.toUnidadEntityList(): List<UnidadEntity> = this.mapNotNull { it?.toEntity() }

fun Usuario.toEntity(): UsuarioEntity {
    return UsuarioEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombreUsuario = this.nombreUsuario,
        nombreCompleto = this.nombreCompleto,
        rolId = this.rol?.id,
        sucursalId = this.sucursal?.id,
        vendedorId = this.vendedor?.id,
        email = null // CAMBIO: El modelo no tiene `email`.
    )
}
fun List<Usuario?>.toUsuarioEntityList(): List<UsuarioEntity> = this.mapNotNull { it?.toEntity() }

fun Vendedor.toEntity(): VendedorEntity {
    return VendedorEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre,
        porcentajeComision = this.porcentajeComision ?: 0.0
    )
}
fun List<Vendedor?>.toVendedorEntityList(): List<VendedorEntity> = this.mapNotNull { it?.toEntity() }

fun ComprobanteConDetalles.toComprobanteConDetalle(): ComprobanteConDetalle {
    // 1. Convierte la parte principal (ComprobanteEntity) al modelo de dominio.
    val domainComprobante = this.comprobante.toDomainModel()

    // 2. Convierte y asigna las entidades relacionadas.
    val domainCliente = this.cliente?.toDomainModel()
    val domainVendedor = this.vendedor?.toDomainModel()
    val domainTipoComprobante = this.tipoComprobante?.toDomainModel() // <-- ¡AQUÍ SE CARGA EL TIPO!
    // Mapeamos las entidades de pago a los modelos de dominio.
    // NOTA: Esto es una simplificación. Idealmente, aquí se buscaría el objeto `FormaPago` completo.
    val formasDePago = this.pagos.map { pagoEntity ->
        FormaPagoComprobante(
            id = pagoEntity.formaPagoId,
            nombre = "Forma de Pago ID: ${pagoEntity.formaPagoId}", // Placeholder
            porcentaje = 0, // Placeholder
            importe = String.format(Locale.US, "%.2f", pagoEntity.importe),
            tipoFormaPago = TipoFormaPago() // Placeholder
        )
    }

    // Mapeamos las entidades de promoción a los modelos de dominio.
    // NOTA: Aquí también se necesitaría buscar el objeto `Promocion` completo.
    val promociones = this.promociones.map { promoEntity ->
        Promocion().apply { id = promoEntity.promocionId }
    }

    // Asignamos las listas mapeadas al comprobante de dominio.
    domainComprobante.formas_de_pago = formasDePago
    domainComprobante.promociones = promociones
    return ComprobanteConDetalle(
        comprobante = domainComprobante,
        cliente = domainCliente,
        vendedor = domainVendedor,
        tipoComprobante = domainTipoComprobante
    )
}


fun ProvinciaConDetalles.toDomainModel(): Provincia {
    val provinciaModel = this.provincia.toDomainModel()
    provinciaModel.pais = this.pais?.toDomainModel()
    return provinciaModel
}

fun UsuarioConDetalles.toDomainModel(): Usuario {
    val usuarioModel = this.usuario.toDomainModel()
    usuarioModel.rol = this.rol?.toDomainModel()
    usuarioModel.sucursal = this.sucursal?.toDomainModel()
    usuarioModel.vendedor = this.vendedor?.toDomainModel()
    return usuarioModel
}

fun Comprobante.toUploadRequest(): ComprobanteUploadRequest {
    val promocionesRequest = this.promociones?.map { PromocionRequest(id = it.id) }
    val pagosRequest = this.formas_de_pago.map {
        FormaPagoRequest(
            id = it.id,
            importe = it.importe.toDoubleOrNull() ?: 0.0
        )
    }
    return ComprobanteUploadRequest(
        numero = this.numero,
        cuotas = this.cuotas,
        clienteId = this.clienteId,
        remito = this.remito,
        persona = this.persona,
        provinciaId = this.provinciaId,
        fecha = this.fecha,
        fechaBaja = this.fechaBaja,
        motivoBaja = this.motivoBaja,
        hora = this.hora,
        fechaProceso = this.fechaProceso,
        letra = this.letra,
        numeroFactura = this.numeroFactura,
        prefijoFactura = this.prefijoFactura,
        operacionNegocioId = this.operacionNegocioId,
        retencionIva = this.retencionIva,
        retencionIibb = this.retencionIibb,
        retencionGanancias = this.retencionGanancias,
        porcentajeGanancias = this.porcentajeGanancias,
        porcentajeIibb = this.porcentajeIibb,
        porcentajeIva = this.porcentajeIva,
        noGravado = this.noGravado,
        importeIva = this.importeIva,
        total = this.total,
        totalPagado = this.totalPagado,
        condicionVentaId = this.condicionVentaId,
        descripcionFlete = this.descripcionFlete,
        vendedorId = this.vendedorId,
        recibo = this.recibo,
        observaciones1 = this.observaciones1,
        observaciones2 = this.observaciones2,
        observaciones3 = this.observaciones3,
        observaciones4 = this.observaciones4,
        descuento = this.descuento,
        descuento1 = this.descuento1,
        descuento2 = this.descuento2,
        descuento3 = this.descuento3,
        descuento4 = this.descuento4,
        iva2 = this.iva2,
        impresa = this.impresa,
        cancelado = this.cancelado,
        nombreCliente = this.nombreCliente,
        direccionCliente = this.direccionCliente,
        localidadCliente = this.localidadCliente,
        garantia = this.garantia,
        concepto = this.concepto,
        notas = this.notas,
        lineaPagoUltima = this.lineaPagoUltima,
        relacionTk = this.relacionTk,
        totalIibb = this.totalIibb,
        importeIibb = this.importeIibb,
        provinciaCategoriaIibbId = this.provinciaCategoriaIibbId,
        importeRetenciones = this.importeRetenciones,
        provinciaIvaProveedorId = this.provinciaIvaProveedorId,
        gananciasProveedorId = this.gananciasProveedorId,
        importeGanancias = this.importeGanancias,
        numeroIibb = this.numeroIibb,
        numeroGanancias = this.numeroGanancias,
        gananciasProveedor = this.gananciasProveedor,
        cae = this.cae,
        fechaVencimiento = this.fechaVencimiento,
        remitoCliente = this.remitoCliente,
        textoDolares = this.textoDolares,
        comprobanteFinal = this.comprobanteFinal,
        numeroGuia1 = this.numeroGuia1,
        numeroGuia2 = this.numeroGuia2,
        numeroGuia3 = this.numeroGuia3,
        tipoAlicuota1 = this.tipoAlicuota1,
        tipoAlicuota2 = this.tipoAlicuota2,
        tipoAlicuota3 = this.tipoAlicuota3,
        importeIva105 = this.importeIva105,
        importeIva21 = this.importeIva21,
        importeIva0 = this.importeIva0,
        noGravadoIva105 = this.noGravadoIva105,
        noGravadoIva21 = this.noGravadoIva21,
        noGravadoIva0 = this.noGravadoIva0,
        direccionEntrega = this.direccionEntrega,
        fechaEntrega = this.fechaEntrega,
        horaEntrega = this.horaEntrega,
        puntoVenta = this.puntoVenta,
        tipoFactura = this.tipoFactura,
        tipoDocumento = this.tipoDocumento,
        numeroDeDocumento = this.numeroDeDocumento,
        qr = this.qr,
        comprobanteIdBaja = this.comprobanteIdBaja,
        sucursalId = this.sucursalId,
        descuentoTotal = this.descuentoTotal,
        incrementoTotal = this.incrementoTotal,
        tipoComprobanteId = this.tipoComprobanteId,
        // --- CAMPOS MAPEADOS ---
        promociones = promocionesRequest,
        formas_de_pago = pagosRequest
    )
}

fun RenglonComprobante.toUploadRequest(comprobanteServerId: Int): RenglonUploadRequest {
    return RenglonUploadRequest(

        productoId = this.producto.id,
        descripcion = this.descripcion,
        cantidad = this.cantidad,
        precioUnitario = this.precioUnitario,
        tasaIva = this.tasaIva,
        totalLinea = this.totalLinea
    )
}

fun ClienteConDetalles.toDomainModel(): Cliente {
    val domainModel = this.cliente.toDomainModel()
    domainModel.localId = this.cliente.id
    domainModel.tipoDocumento = this.tipoDocumento?.toDomainModel()
    domainModel.tipoIva = this.tipoIva?.toDomainModel()
    domainModel.localidad = this.localidad?.toDomainModel()
    // Si la localidad tiene una provincia, también la mapeamos
    this.localidad?.provinciaId?.let { provId ->
        // Este es un caso especial. Asumimos que la provincia ya está en el objeto Localidad
        // o la cargamos aquí si es necesario, aunque lo ideal es que la relación anidada funcione.
        // El mapper de LocalidadConDetalles debería encargarse de esto.
        // Por simplicidad, asumimos que el modelo de dominio de Localidad ya tiene la Provincia.
    }
    domainModel.provincia = this.provincia?.toDomainModel()
    domainModel.categoria = this.categoria?.toDomainModel()
    domainModel.vendedores = this.vendedor?.toDomainModel()
    return domainModel
}

fun Cliente.toUploadRequest(): ClienteUploadRequest {
    return ClienteUploadRequest(

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

fun Agrupacion.toUploadRequest(): AgrupacionUploadRequest = AgrupacionUploadRequest(numero, nombre, color, icono)
fun Categoria.toUploadRequest(): CategoriaUploadRequest = CategoriaUploadRequest(nombre, seImprime)
fun CierreCaja.toUploadRequest(): CierreCajaUploadRequest = CierreCajaUploadRequest(fecha, totalVentas, totalGastos, efectivoInicial, efectivoFinal, tipoCajaId, usuario?.id)
fun Familia.toUploadRequest(): FamiliaUploadRequest = FamiliaUploadRequest(numero.toString(), nombre)
fun FormaPago.toUploadRequest(): FormaPagoUploadRequest = FormaPagoUploadRequest(nombre, porcentaje,activa, tipoFormaPago?.id)
fun Localidad.toUploadRequest(): LocalidadUploadRequest = LocalidadUploadRequest(nombre, codigoPostal, provincia?.id)
fun Moneda.toUploadRequest(): MonedaUploadRequest = MonedaUploadRequest(simbolo, nombre, cotizacion)
fun Pais.toUploadRequest(): PaisUploadRequest = PaisUploadRequest(nombre)
fun Producto.toUploadRequest(): ProductoUploadRequest = ProductoUploadRequest(codigo, descripcion, descripcionAmpliada, stock, stockMinimo, stockPedido, codigoBarra, articuloActivado, productoBalanza, precio1, precio2, precio3,  moneda?.id, tasaIva?.id, incluyeIva, impuestoInterno, tipoImpuestoInterno, precioCosto, fraccionado, rg5329_23, activo, textoPanel, iibb, codigoBarra2, oferta, margenGanancia, favorito, familia?.id, agrupacion?.id, proveedor?.id, tipo?.id, unidad?.id)
fun Promocion.toUploadRequest(): PromocionUploadRequest = PromocionUploadRequest(nombre, descripcion, porcentaje)
fun Provincia.toUploadRequest(): ProvinciaUploadRequest = ProvinciaUploadRequest(nombre, pais?.id)
fun Rol.toUploadRequest(): RolUploadRequest = RolUploadRequest(nombre, descripcion)
fun Sucursal.toUploadRequest(): SucursalUploadRequest = SucursalUploadRequest(nombre, direccion)
fun TasaIva.toUploadRequest(): TasaIvaUploadRequest = TasaIvaUploadRequest(nombre, tasa)
fun TipoComprobante.toUploadRequest(): TipoComprobanteUploadRequest = TipoComprobanteUploadRequest(nombre, numero)
fun TipoDocumento.toUploadRequest(): TipoDocumentoUploadRequest = TipoDocumentoUploadRequest(nombre, null) // Asumiendo que 'abrev' no se gestiona en la app
fun TipoFormaPago.toUploadRequest(): TipoFormaPagoUploadRequest = TipoFormaPagoUploadRequest(nombre)

fun Tipo.toUploadRequest(): TipoUploadRequest = TipoUploadRequest(nombre, numero)
fun Unidad.toUploadRequest(): UnidadUploadRequest = UnidadUploadRequest(nombre, simbolo)
fun Usuario.toUploadRequest(): UsuarioUploadRequest = UsuarioUploadRequest(nombreUsuario, null, nombreCompleto, activo, rol?.id, sucursal?.id, vendedor?.id) // Se envía null en password para no cambiarlo
fun Vendedor.toUploadRequest(): VendedorUploadRequest = VendedorUploadRequest(nombre, direccion, telefono, porcentajeComision, fechaIngreso)
fun Proveedor.toUploadRequest(): ProveedorUploadRequest {
    return ProveedorUploadRequest(
        razonSocial = this.razonSocial,
        direccion = this.direccion,
        localidadId = this.localidad?.id,
        telefono = this.telefono,
        email = this.email,
        tipoIvaId = this.tipoIva?.id,
        cuit = this.cuit,
        categoriaId = this.categoria?.id,
        subcategoriaId = this.subcategoria?.id,
        fechaUltimaCompra = this.fechaUltimaCompra,
        fechaUltimoPago = this.fechaUltimoPago,
        saldoActual = this.saldoActual
    )
}

fun TipoIVA.toUploadRequest(): TipoIvaUploadRequest {
    return TipoIvaUploadRequest(
        nombre = this.nombre,

    )
}

fun Pago.toEntity(comprobanteLocalId: Long): ComprobantePagoEntity {
    return ComprobantePagoEntity(
        comprobanteLocalId = comprobanteLocalId,
        formaPagoId = this.formaPago.id,
        importe = this.monto,
        syncStatus = SyncStatus.CREATED // Los pagos siempre se crean localmente primero
    )
}