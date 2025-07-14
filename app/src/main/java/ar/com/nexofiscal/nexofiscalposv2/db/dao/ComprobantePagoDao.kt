// main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/ComprobantePagoDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobantePagoEntity

@Dao
interface ComprobantePagoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(pagos: List<ComprobantePagoEntity>)

    @Query("DELETE FROM comprobante_promociones WHERE comprobanteLocalId = :comprobanteId")
    fun deletePromocionesForComprobante(comprobanteId: Int)

    // --- INICIO DE LA MODIFICACIÓN ---
    @Query("SELECT * FROM comprobante_pagos WHERE comprobanteLocalId = :comprobanteLocalId")
    suspend fun getByComprobanteLocalId(comprobanteLocalId: Long): List<ComprobantePagoEntity>
    // --- FIN DE LA MODIFICACIÓN ---
}