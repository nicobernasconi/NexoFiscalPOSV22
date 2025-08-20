package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    viewName = "vw_informe_cierre",
    value = """
        SELECT 
            cc.id AS id,
            cc.fecha AS fecha,
            (
                SELECT IFNULL(SUM(cfp.importe), 0)
                FROM comprobantes cp
                JOIN comprobante_pagos cfp ON cfp.comprobanteLocalId = cp.id
                LEFT JOIN formas_pago fp ON fp.serverId = cfp.formaPagoId
                WHERE cp.cierreCajaId = cc.id
                  AND cp.fechaBaja IS NULL
                  AND (cp.tipoComprobanteId IN (1,3))
                  AND (fp.tipoFormaPagoId IS NULL OR fp.tipoFormaPagoId <> 2)
            ) AS total_ventas,
            (
                SELECT IFNULL(SUM(g.monto), 0)
                FROM gastos g
                WHERE g.cierreCajaId = cc.id
                  AND (g.syncStatus IS NULL OR g.syncStatus <> 'DELETED')
            ) AS total_gastos,
            cc.efectivoInicial AS efectivo_inicial,
            cc.efectivoFinal AS efectivo_final,
            cc.usuarioId AS usuario_id,
            u.nombreCompleto AS nombre_usuario,
            CAST(NULL AS INTEGER) AS empresa_id,
            CAST(NULL AS TEXT) AS comentarios,
            COALESCE((
                SELECT '{' || GROUP_CONCAT('"' || IFNULL(tfp.nombre_forma_pago,'SIN ASIGNAR') || '":' || printf('%.2f', tfp.total_forma_pago)) || '}'
                FROM (
                    SELECT fp2.nombre AS nombre_forma_pago, SUM(cfp2.importe) AS total_forma_pago
                    FROM comprobantes cp2
                    JOIN comprobante_pagos cfp2 ON cfp2.comprobanteLocalId = cp2.id
                    LEFT JOIN formas_pago fp2 ON fp2.serverId = cfp2.formaPagoId
                    WHERE cp2.cierreCajaId = cc.id
                      AND cp2.fechaBaja IS NULL
                      AND (cp2.tipoComprobanteId IN (1,3))
                      AND (fp2.tipoFormaPagoId IS NULL OR fp2.tipoFormaPagoId <> 2)
                    GROUP BY fp2.nombre
                ) AS tfp
            ), '{}') AS ventas_por_forma_pago,
            COALESCE((
                SELECT '{' || GROUP_CONCAT('"' || IFNULL(gt.nombre_tipo,'SIN TIPO') || '":' || printf('%.2f', gt.total_tipo)) || '}'
                FROM (
                    SELECT 
                        COALESCE(
                            g.tipoGasto,
                            (SELECT t.nombre FROM tipos_gastos t WHERE t.id = g.tipoGastoId),
                            'SIN TIPO'
                        ) AS nombre_tipo,
                        SUM(g.monto) AS total_tipo
                    FROM gastos g
                    WHERE g.cierreCajaId = cc.id
                      AND (g.syncStatus IS NULL OR g.syncStatus <> 'DELETED')
                    GROUP BY nombre_tipo
                ) AS gt
            ), '{}') AS gastos_por_tipo
        FROM cierres_caja cc
        LEFT JOIN usuarios u ON u.id = cc.usuarioId
        ORDER BY cc.id DESC
    """
)
data class CierreCajaInformeView(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "fecha") val fecha: String?,
    @ColumnInfo(name = "total_ventas") val totalVentas: Double?,
    @ColumnInfo(name = "total_gastos") val totalGastos: Double?,
    @ColumnInfo(name = "efectivo_inicial") val efectivoInicial: Double?,
    @ColumnInfo(name = "efectivo_final") val efectivoFinal: Double?,
    @ColumnInfo(name = "usuario_id") val usuarioId: Int?,
    @ColumnInfo(name = "nombre_usuario") val nombreUsuario: String?,
    @ColumnInfo(name = "empresa_id") val empresaId: Int?,
    @ColumnInfo(name = "comentarios") val comentarios: String?,
    @ColumnInfo(name = "ventas_por_forma_pago") val ventasPorFormaPago: String,
    @ColumnInfo(name = "gastos_por_tipo") val gastosPorTipo: String
)
