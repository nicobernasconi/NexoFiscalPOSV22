// main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/ComprobantePromocionDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobantePromocionEntity

@Dao
interface ComprobantePromocionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(promociones: List<ComprobantePromocionEntity>)

    @Query("DELETE FROM comprobante_promociones WHERE comprobanteLocalId = :comprobanteId")
    fun deletePromocionesForComprobante(comprobanteId: Int)

    // --- INICIO DE LA MODIFICACIÓN ---
    @Query("SELECT * FROM comprobante_promociones WHERE comprobanteLocalId = :comprobanteLocalId")
    suspend fun getByComprobanteLocalId(comprobanteLocalId: Long): List<ComprobantePromocionEntity>
    // --- FIN DE LA MODIFICACIÓN ---
}