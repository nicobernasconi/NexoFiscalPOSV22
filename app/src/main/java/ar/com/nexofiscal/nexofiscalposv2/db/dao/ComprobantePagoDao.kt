package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobantePagoEntity

@Dao
interface ComprobantePagoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertAll(pagos: List<ComprobantePagoEntity>)
}