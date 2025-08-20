package ar.com.nexofiscal.nexofiscalposv2.db.entity

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    viewName = "vw_cierres_caja",
    value = """
        SELECT 
            cc.id AS id,
            cc.fecha AS fecha,
            cc.totalGastos AS total_gastos,
            cc.efectivoInicial AS efectivo_inicial,
            cc.efectivoFinal AS efectivo_final,
            cc.usuarioId AS usuario_id,
            cc.tipoCajaId AS tipo_caja_id,
            u.nombreCompleto AS usuario_nombre_completo,
            cc.totalVentas AS total_ventas,
            CAST(NULL AS INTEGER) AS empresa_id,
            cc.comentarios AS comentarios,
            u.sucursalId AS sucursal_id
        FROM cierres_caja cc
        LEFT JOIN usuarios u ON u.id = cc.usuarioId
        WHERE cc.totalVentas IS NOT NULL
        
        UNION ALL
        
        SELECT 
            cc.id AS id,
            cc.fecha AS fecha,
            cc.totalGastos AS total_gastos,
            cc.efectivoInicial AS efectivo_inicial,
            cc.efectivoFinal AS efectivo_final,
            cc.usuarioId AS usuario_id,
            cc.tipoCajaId AS tipo_caja_id,
            u.nombreCompleto AS usuario_nombre_completo,
            (
                COALESCE((
                    SELECT SUM(cfp.importe)
                    FROM comprobantes cp
                    JOIN comprobante_pagos cfp ON cfp.comprobanteLocalId = cp.id
                    LEFT JOIN formas_pago fp ON fp.id = cfp.formaPagoId
                    WHERE cp.cierreCajaId = cc.id
                      AND cp.fechaBaja IS NULL
                      AND cp.tipoComprobanteId IN (1,3)
                      AND (fp.tipoFormaPagoId IS NULL OR fp.tipoFormaPagoId <> 2)
                ), 0)
            ) AS total_ventas,
            CAST(NULL AS INTEGER) AS empresa_id,
            cc.comentarios AS comentarios,
            u.sucursalId AS sucursal_id
        FROM cierres_caja cc
        LEFT JOIN usuarios u ON u.id = cc.usuarioId
        WHERE cc.totalVentas IS NULL
    """
)
data class CierreCajaResumenView(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "fecha") val fecha: String?,
    @ColumnInfo(name = "total_gastos") val totalGastos: Double?,
    @ColumnInfo(name = "efectivo_inicial") val efectivoInicial: Double?,
    @ColumnInfo(name = "efectivo_final") val efectivoFinal: Double?,
    @ColumnInfo(name = "usuarioId") val usuarioId: Int?,
    @ColumnInfo(name = "tipo_caja_id") val tipoCajaId: Int?,
    @ColumnInfo(name = "usuario_nombre_completo") val usuarioNombreCompleto: String?,
    @ColumnInfo(name = "total_ventas") val totalVentas: Double?,
    @ColumnInfo(name = "empresa_id") val empresaId: Int?,
    @ColumnInfo(name = "comentarios") val comentarios: String?,
    @ColumnInfo(name = "sucursal_id") val sucursalId: Int?
)
