package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName


data class AgrupacionUploadRequest(
    @SerializedName("numero") val numero: Int?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("icono") val icono: String?
)

data class CategoriaUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("se_imprime") val seImprime: Int?
)

data class CierreCajaUploadRequest(
    @SerializedName("fecha") val fecha: String?,
    @SerializedName("total_ventas") val totalVentas: Double?,
    @SerializedName("total_gastos") val totalGastos: Double?,
    @SerializedName("efectivo_inicial") val efectivoInicial: Double?,
    @SerializedName("efectivo_final") val efectivoFinal: Double?,
    @SerializedName("tipo_caja_id") val tipoCajaId: Int?,
    @SerializedName("usuario_id") val usuarioId: Int?
)

data class FamiliaUploadRequest(
    @SerializedName("numero") val numero: String?,
    @SerializedName("nombre") val nombre: String?
)

data class FormaPagoUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("porcentaje") val porcentaje: Int?,
    @SerializedName("activa") val activa: Int?,
    @SerializedName("tipo_forma_pago_id") val tipoFormaPagoId: Int?
)

data class LocalidadUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("codigo_postal") val codigoPostal: String?,
    @SerializedName("provincia_id") val provinciaId: Int?
)

data class MonedaUploadRequest(
    @SerializedName("simbolo") val simbolo: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("cotizacion") val cotizacion: Double?
)

data class PaisUploadRequest(
    @SerializedName("nombre") val nombre: String?
)

data class ProductoUploadRequest(
    @SerializedName("codigo") val codigo: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("descripcion_ampliada") val descripcionAmpliada: String?,
    @SerializedName("stock") val stock: Int?,
    @SerializedName("stock_minimo") val stockMinimo: Int?,
    @SerializedName("stock_pedido") val stockPedido: Int?,
    @SerializedName("codigo_barra") val codigoBarra: String?,
    @SerializedName("articulo_activado") val articuloActivado: Boolean?,
    @SerializedName("producto_balanza") val productoBalanza: Int?,
    @SerializedName("precio1") val precio1: Double?,
    @SerializedName("precio2") val precio2: Double?,
    @SerializedName("precio3") val precio3: Double?,
    @SerializedName("moneda_id") val monedaId: Int?,
    @SerializedName("tasa_iva_id") val tasaIvaId: Int?,
    @SerializedName("incluye_iva") val incluyeIva: Int?,
    @SerializedName("impuesto_interno") val impuestoInterno: Double?,
    @SerializedName("tipo_impuesto_interno") val tipoImpuestoInterno: Int?,
    @SerializedName("precio_costo") val precioCosto: Double?,
    @SerializedName("fraccionado") val fraccionado: Int?,
    @SerializedName("rg5329_23") val rg5329_23: Int?,
    @SerializedName("activo") val activo: Int?,
    @SerializedName("texto_panel") val textoPanel: String?,
    @SerializedName("iibb") val iibb: Double?,
    @SerializedName("codigo_barra2") val codigoBarra2: String?,
    @SerializedName("oferta") val oferta: Int?,
    @SerializedName("margen_ganancia") val margenGanancia: Double?,
    @SerializedName("favorito") val favorito: Int?,
    @SerializedName("familia_id") val familiaId: Int?,
    @SerializedName("agrupacion_id") val agrupacionId: Int?,
    @SerializedName("proveedor_id") val proveedorId: Int?,
    @SerializedName("tipo_id") val tipoId: Int?,
    @SerializedName("unidad_id") val unidadId: Int?
)

data class PromocionUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("porcentaje") val porcentaje: Int?
)

data class ProvinciaUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("pais_id") val paisId: Int?
)

data class RolUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?
)

data class SucursalUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("direccion") val direccion: String?
)

data class TasaIvaUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("tasa") val tasa: Double?
)

data class TipoComprobanteUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("numero") val numero: Int?
)

data class TipoDocumentoUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("abrev") val abrev: String?
)

data class TipoFormaPagoUploadRequest(
    @SerializedName("nombre") val nombre: String?
)

data class TipoIvaUploadRequest(
    @SerializedName("nombre") val nombre: String?

)

data class TipoUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("numero") val numero: Int?
)

data class UnidadUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("simbolo") val simbolo: String?
)

data class UsuarioUploadRequest(
    @SerializedName("nombre_usuario") val nombreUsuario: String?,
    @SerializedName("password") val password: String?, // Enviar solo si se quiere cambiar
    @SerializedName("nombre_completo") val nombreCompleto: String?,
    @SerializedName("activo") val activo: Int?,
    @SerializedName("rol_id") val rolId: Int?,
    @SerializedName("sucursal_id") val sucursalId: Int?,
    @SerializedName("vendedor_id") val vendedorId: Int?
)

data class VendedorUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("direccion") val direccion: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("porcentaje_comision") val porcentajeComision: Double?,
    @SerializedName("fecha_ingreso") val fechaIngreso: String?
)

data class RenglonUploadRequest(
    @SerializedName("producto_id") val productoId: Int,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("cantidad") val cantidad: Double,
    @SerializedName("precio_unitario") val precioUnitario: Double,
    @SerializedName("tasa_iva") val tasaIva: Double,
    @SerializedName("total_linea") val totalLinea: String
)

data class ComprobanteUploadRequest(
    @SerializedName("numero") val numero: Int?,
    @SerializedName("cuotas") val cuotas: Int?,
    @SerializedName("cliente_id") val clienteId: Int,
    @SerializedName("remito") val remito: String?,
    @SerializedName("persona") val persona: String?,
    @SerializedName("provincia_id") val provinciaId: Int?,
    @SerializedName("fecha") val fecha: String?,
    @SerializedName("fecha_baja") val fechaBaja: String?,
    @SerializedName("motivo_baja") val motivoBaja: String?,
    @SerializedName("hora") val hora: String?,
    @SerializedName("fecha_proceso") val fechaProceso: String?,
    @SerializedName("letra") val letra: String?,
    @SerializedName("numero_factura") val numeroFactura: Int?,
    @SerializedName("prefijo_factura") val prefijoFactura: String?,
    @SerializedName("operacion_negocio_id") val operacionNegocioId: Int?,
    @SerializedName("retencion_iva") val retencionIva: Double?,
    @SerializedName("retencion_iibb") val retencionIibb: Double?,
    @SerializedName("retencion_ganancias") val retencionGanancias: Double?,
    @SerializedName("porcentaje_ganancias") val porcentajeGanancias: Double?,
    @SerializedName("porcentaje_iibb") val porcentajeIibb: Double?,
    @SerializedName("porcentaje_iva") val porcentajeIva: Double?,
    @SerializedName("no_gravado") val noGravado: Double?,
    @SerializedName("importe_iva") val importeIva: Double?,
    @SerializedName("total") val total: String?,
    @SerializedName("total_pagado") val totalPagado: Double?,
    @SerializedName("condicion_venta_id") val condicionVentaId: Int?,
    @SerializedName("descripcion_flete") val descripcionFlete: String?,
    @SerializedName("vendedor_id") val vendedorId: Int?,
    @SerializedName("recibo") val recibo: String?,
    @SerializedName("observaciones_1") val observaciones1: String?,
    @SerializedName("observaciones_2") val observaciones2: String?,
    @SerializedName("observaciones_3") val observaciones3: String?,
    @SerializedName("observaciones_4") val observaciones4: String?,
    @SerializedName("descuento") val descuento: Double?,
    @SerializedName("descuento_1") val descuento1: Double?,
    @SerializedName("descuento_2") val descuento2: Double?,
    @SerializedName("descuento_3") val descuento3: Double?,
    @SerializedName("descuento_4") val descuento4: Double?,
    @SerializedName("iva_2") val iva2: Double?,
    @SerializedName("impresa") val impresa: Boolean?,
    @SerializedName("cancelado") val cancelado: Boolean?,
    @SerializedName("nombre_cliente") val nombreCliente: String?,
    @SerializedName("direccion_cliente") val direccionCliente: String?,
    @SerializedName("localidad_cliente") val localidadCliente: String?,
    @SerializedName("garantia") val garantia: String?,
    @SerializedName("concepto") val concepto: Int?,
    @SerializedName("notas") val notas: String?,
    @SerializedName("linea_pago_ultima") val lineaPagoUltima: String?,
    @SerializedName("relacion_tk") val relacionTk: String?,
    @SerializedName("total_iibb") val totalIibb: Double?,
    @SerializedName("importe_iibb") val importeIibb: Double?,
    @SerializedName("provincia_categoria_iibb_id") val provinciaCategoriaIibbId: Int?,
    @SerializedName("importe_retenciones") val importeRetenciones: Double?,
    @SerializedName("provincia_iva_proveedor_id") val provinciaIvaProveedorId: Int?,
    @SerializedName("ganancias_proveedor_id") val gananciasProveedorId: Int?,
    @SerializedName("importe_ganancias") val importeGanancias: Double?,
    @SerializedName("numero_iibb") val numeroIibb: String?,
    @SerializedName("numero_ganancias") val numeroGanancias: String?,
    @SerializedName("ganancias_proveedor") val gananciasProveedor: String?,
    @SerializedName("cae") val cae: String?,
    @SerializedName("fecha_vencimiento") val fechaVencimiento: String?,
    @SerializedName("remito_cliente") val remitoCliente: String?,
    @SerializedName("texto_dolares") val textoDolares: String?,
    @SerializedName("comprobante_final") val comprobanteFinal: String?,
    @SerializedName("numero_guia_1") val numeroGuia1: String?,
    @SerializedName("numero_guia_2") val numeroGuia2: String?,
    @SerializedName("numero_guia_3") val numeroGuia3: String?,
    @SerializedName("tipo_alicuota_1") val tipoAlicuota1: Double?,
    @SerializedName("tipo_alicuota_2") val tipoAlicuota2: Double?,
    @SerializedName("tipo_alicuota_3") val tipoAlicuota3: Double?,
    @SerializedName("importe_iva_105") val importeIva105: Double?,
    @SerializedName("importe_iva_21") val importeIva21: Double?,
    @SerializedName("importe_iva_0") val importeIva0: Double?,
    @SerializedName("no_gravado_iva_105") val noGravadoIva105: Double?,
    @SerializedName("no_gravado_iva_21") val noGravadoIva21: Double?,
    @SerializedName("no_gravado_iva_0") val noGravadoIva0: Double?,
    @SerializedName("direccion_entrega") val direccionEntrega: String?,
    @SerializedName("fecha_entrega") val fechaEntrega: String?,
    @SerializedName("hora_entrega") val horaEntrega: String?,
    @SerializedName("punto_venta") val puntoVenta: Int?,
    @SerializedName("tipo_factura") val tipoFactura: Int?,
    @SerializedName("tipo_documento") val tipoDocumento: Int?,
    @SerializedName("numero_de_documento") val numeroDeDocumento: Long?,
    @SerializedName("qr") val qr: String?,
    @SerializedName("comprobante_id_baja") val comprobanteIdBaja: String?,
    @SerializedName("sucursal_id") val sucursalId: Int?,
    @SerializedName("descuento_total") val descuentoTotal: String?,
    @SerializedName("incremento_total") val incrementoTotal: String?,
    @SerializedName("tipo_comprobante_id") val tipoComprobanteId: Int?,
    @SerializedName("promociones") val promociones: List<PromocionRequest>?,
    @SerializedName("formas_de_pago") val formas_de_pago: List<FormaPagoRequest>?

    )

data class PromocionRequest(
    @SerializedName("id") val id: Int
)

data class FormaPagoRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("importe") val importe: Double
)

data class ClienteUploadRequest(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("cuit") val cuit: String?,
    @SerializedName("tipo_documento_id") val tipoDocumentoId: Int?,
    @SerializedName("numero_documento") val numeroDocumento: String?,
    @SerializedName("direccion_comercial") val direccionComercial: String?,
    @SerializedName("direccion_entrega") val direccionEntrega: String?,
    @SerializedName("localidad_id") val localidadId: Int?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("celular") val celular: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("contacto") val contacto: String?,
    @SerializedName("telefono_contacto") val telefonoContacto: String?,
    @SerializedName("categoria_id") val categoriaId: Int?,
    @SerializedName("vendedores_id") val vendedoresId: Int?,
    @SerializedName("porcentaje_descuento") val porcentajeDescuento: Double?,
    @SerializedName("limite_credito") val limiteCredito: Double?,
    @SerializedName("saldo_inicial") val saldoInicial: Double?,
    @SerializedName("saldo_actual") val saldoActual: Double?,
    @SerializedName("fecha_ultima_compra") val fechaUltimaCompra: String?,
    @SerializedName("fecha_ultimo_pago") val fechaUltimoPago: String?,
    @SerializedName("percepcion_iibb") val percepcionIibb: Double?,
    @SerializedName("desactivado") val desactivado: Boolean?,
    @SerializedName("tipo_iva_id") val tipoIvaId: Int?,
    @SerializedName("provincia_id") val provinciaId: Int?
)

data class ProveedorUploadRequest(
    @SerializedName("razon_social") val razonSocial: String?,
    @SerializedName("direccion") val direccion: String?,
    @SerializedName("localidad_id") val localidadId: Int?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("tipo_iva_id") val tipoIvaId: Int?,
    @SerializedName("cuit") val cuit: String?,
    @SerializedName("categoria_id") val categoriaId: Int?,
    @SerializedName("subcategoria_id") val subcategoriaId: Int?,
    @SerializedName("fecha_ultima_compra") val fechaUltimaCompra: String?,
    @SerializedName("fecha_ultimo_pago") val fechaUltimoPago: String?,
    @SerializedName("saldo_actual") val saldoActual: Double?
)
