package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoGastoEntity

@Dao
interface TipoGastoDao {
    @Query("SELECT * FROM tipos_gastos ORDER BY id ASC")
    suspend fun getAll(): List<TipoGastoEntity>

    @Query("SELECT * FROM tipos_gastos WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): TipoGastoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TipoGastoEntity>)
}
