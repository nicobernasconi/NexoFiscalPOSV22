// main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/ComprobantePagoDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobantePagoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FormaPagoTotal

@Dao
interface ComprobantePagoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(pagos: List<ComprobantePagoEntity>)

    @Query("DELETE FROM comprobante_pagos WHERE comprobanteLocalId = :comprobanteId")
    fun deletePagosForComprobante(comprobanteId: Int)

    @Query("SELECT * FROM comprobante_pagos WHERE comprobanteLocalId = :comprobanteLocalId")
    suspend fun getByComprobanteLocalId(comprobanteLocalId: Int): List<ComprobantePagoEntity>

    @Query(
        """
        SELECT cfp.formaPagoId AS formaPagoId,
               fp.nombre        AS nombre,
               SUM(cfp.importe) AS total
        FROM comprobantes cp
        JOIN comprobante_pagos cfp ON cfp.comprobanteLocalId = cp.id
        LEFT JOIN formas_pago fp ON fp.serverId = cfp.formaPagoId
        WHERE cp.cierreCajaId = :cierreId
          AND cp.fechaBaja IS NULL
          AND (fp.tipoFormaPagoId IS NULL OR fp.tipoFormaPagoId <> 2)
        GROUP BY cfp.formaPagoId, fp.nombre
        ORDER BY total DESC
        """
    )
    suspend fun getTotalesPorFormaPagoPorCierre(cierreId: Int): List<FormaPagoTotal>

    @Query(
        """
        SELECT IFNULL(SUM(cfp.importe), 0)
        FROM comprobantes cp
        JOIN comprobante_pagos cfp ON cfp.comprobanteLocalId = cp.id
        LEFT JOIN formas_pago fp ON fp.serverId = cfp.formaPagoId
        WHERE cp.cierreCajaId = :cierreId
          AND cp.fechaBaja IS NULL
          AND (fp.tipoFormaPagoId IS NULL OR fp.tipoFormaPagoId <> 2)
        """
    )
    suspend fun getTotalPagosPorCierre(cierreId: Int): Double
}