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
}

