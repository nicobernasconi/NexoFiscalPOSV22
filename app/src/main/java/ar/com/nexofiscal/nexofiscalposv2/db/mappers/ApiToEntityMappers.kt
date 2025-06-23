package ar.com.nexofiscal.nexofiscalposv2.db.mappers

import ar.com.nexofiscal.nexofiscalposv2.db.entity.*
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ComprobanteConDetalle
import ar.com.nexofiscal.nexofiscalposv2.models.*
import com.google.gson.Gson

// MAPPERS DE ENTIDAD A MODELO DE DOMINIO (API)
// Nota: Se han corregido los mappers para que solo asignen los campos que realmente existen en el modelo de dominio.

fun AgrupacionEntity.toDomainModel(): Agrupacion {
    return Agrupacion(
        id = this.serverId ?: this.id, // Prioriza serverId si existe
        numero = this.numero,
        nombre = this.nombre,
        color = this.color,
        icono = this.icono
    )
}

fun List<AgrupacionEntity>.toAgrupacionDomainModelList(): List<Agrupacion> {
    return this.map { it.toDomainModel() }
}

fun CategoriaEntity.toDomainModel(): Categoria {
    val domain = Categoria()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    domain.seImprime = this.seImprime
    return domain
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
    this.tipoIvaId?.let { domain.tipoIva = TipoIVA().apply { id = it } }
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
        localId = this.id, // CAMBIO: Se asigna el ID local de la entidad
        id = this.serverId ?: this.id, // El ID de dominio sigue siendo el del servidor (o el local como fallback)
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
        cliente = Cliente().apply { id = this@toDomainModel.clienteId },
        vendedor = this.vendedorId?.let { Vendedor().apply { id = it } },
        provincia = this.provinciaId?.let { Provincia().apply { id = it } },
        tipoComprobante = this.tipoComprobanteId?.let { TipoComprobante().apply { id = it } },
        tipoComprobanteId = this.tipoComprobanteId,

    )
}

fun List<ComprobanteEntity>.toComprobanteDomainModelList(): List<Comprobante> {
    return this.map { it.toDomainModel() }
}

fun FamiliaEntity.toDomainModel(): Familia {
    return Familia(
        id = this.serverId ?: this.id,
        numero = this.numero,
        nombre = this.nombre
    )
}

fun List<FamiliaEntity>.toFamiliaDomainModelList(): List<Familia> {
    return this.map { it.toDomainModel() }
}

fun FormaPagoEntity.toDomainModel(): FormaPago {
    val domain = FormaPago()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    domain.porcentaje = this.porcentaje
    this.tipoFormaPagoId?.let { domain.tipoFormaPago = TipoFormaPago().apply { id = it } }
    return domain
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

fun MonedaEntity.toDomainModel(): Moneda {
    val domain = Moneda()
    domain.id = this.serverId ?: this.id
    domain.simbolo = this.simbolo
    domain.nombre = this.nombre
    domain.cotizacion = this.cotizacion
    return domain
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
    domain.localId = this.id // <-- CAMBIO: AÑADIR ESTA LÍNEA para asignar el ID local de la entidad
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

fun List<ProductoEntity>.toProductoDomainModelList(): List<Producto> {
    return this.map { it.toDomainModel() }
}

fun PromocionEntity.toDomainModel(): Promocion {
    val domain = Promocion()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    domain.descripcion = this.descripcion
    domain.porcentaje = this.porcentaje
    return domain
}

fun List<PromocionEntity>.toPromocionDomainModelList(): List<Promocion> {
    return this.map { it.toDomainModel() }
}

fun ProveedorEntity.toDomainModel(): Proveedor {
    val domain = Proveedor()
    domain.id = this.serverId ?: this.id
    domain.razonSocial = this.razonSocial
    domain.direccion = this.direccion
    this.localidadId?.let { domain.localidad = Localidad().apply { id = it } }
    domain.telefono = this.telefono
    domain.email = this.email
    this.tipoIvaId?.let { domain.tipoIva = TipoIVA().apply { id = it } }
    domain.cuit = this.cuit
    this.categoriaId?.let { domain.categoria = Categoria().apply { id = it } }
    this.subcategoriaId?.let { domain.subcategoria = Categoria().apply { id = it } }
    domain.fechaUltimaCompra = this.fechaUltimaCompra
    domain.fechaUltimoPago = this.fechaUltimoPago
    domain.saldoActual = this.saldoActual
    return domain
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

fun TipoDocumentoEntity.toDomainModel(): TipoDocumento {
    val domain = TipoDocumento()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    return domain
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

fun List<TipoFormaPagoEntity>.toTipoFormaPagoDomainModelList(): List<TipoFormaPago> {
    return this.map { it.toDomainModel() }
}

fun TipoIvaEntity.toDomainModel(): TipoIVA {
    val domain = TipoIVA()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    // CAMBIO: Faltaban campos que no existen en la entidad.
    // domain.letraFactura = this.letraFactura
    // domain.porcentaje = this.porcentaje
    return domain
}

fun List<TipoIvaEntity>.toTipoIvaDomainModelList(): List<TipoIVA> {
    return this.map { it.toDomainModel() }
}

fun UnidadEntity.toDomainModel(): Unidad {
    val domain = Unidad()
    domain.id = this.serverId ?: this.id
    domain.nombre = this.nombre
    // CAMBIO: El campo 'simbolo' no existe en UnidadEntity
    // domain.simbolo = this.simbolo
    return domain
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

fun Agrupacion.toEntity(): AgrupacionEntity {
    return AgrupacionEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        numero = this.numero,
        nombre = this.nombre,
        color = this.color,
        icono = this.icono
    )
}
fun List<Agrupacion?>.toAgrupacionEntityList(): List<AgrupacionEntity> = this.mapNotNull { it?.toEntity() }

fun Categoria.toEntity(): CategoriaEntity {
    return CategoriaEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
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
        id = 0,
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
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        numero = this.numero,
        nombre = this.nombre
    )
}
fun List<Familia?>.toFamiliaEntityList(): List<FamiliaEntity> = this.mapNotNull { it?.toEntity() }

fun FormaPago.toEntity(): FormaPagoEntity {
    return FormaPagoEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
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

fun Moneda.toEntity(): MonedaEntity {
    return MonedaEntity(
        id = 0,
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
        id = 0,
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

fun Promocion.toEntity(): PromocionEntity {
    return PromocionEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre,
        descripcion = this.descripcion,
        porcentaje = this.porcentaje
    )
}
fun List<Promocion?>.toPromocionEntityList(): List<PromocionEntity> = this.mapNotNull { it?.toEntity() }

fun Proveedor.toEntity(): ProveedorEntity {
    return ProveedorEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
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

fun TipoDocumento.toEntity(): TipoDocumentoEntity {
    return TipoDocumentoEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre,
        abrev = null, // CAMBIO: El modelo no tiene estos campos, se asigna null.
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

fun TipoIVA.toEntity(): TipoIvaEntity {
    return TipoIvaEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre,
        descripcion = null // CAMBIO: El modelo no tiene `descripcion`, se asigna null.
    )
}
fun List<TipoIVA?>.toTipoIvaEntityList(): List<TipoIvaEntity> = this.mapNotNull { it?.toEntity() }

fun Unidad.toEntity(): UnidadEntity {
    return UnidadEntity(
        id = 0,
        serverId = this.id,
        syncStatus = SyncStatus.SYNCED,
        nombre = this.nombre
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

fun ProductoConDetalles.toDomainModel(): Producto {
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

fun ComprobanteConDetallesEntity.toComprobanteConDetalle(): ComprobanteConDetalle {
    return ComprobanteConDetalle(
        comprobante = this.comprobante.toDomainModel(),
        cliente = this.cliente?.toDomainModel(),
        tipoComprobante = this.tipoComprobante?.toDomainModel()
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


