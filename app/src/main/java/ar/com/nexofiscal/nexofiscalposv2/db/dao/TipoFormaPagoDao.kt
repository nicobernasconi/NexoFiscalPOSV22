// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/TipoFormaPagoDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoFormaPagoEntity

@Dao
interface TipoFormaPagoDao {
    @Query("SELECT * FROM tipos_forma_pago")
    fun getAll(): Flow<List<TipoFormaPagoEntity>>

    @Query("SELECT * FROM tipos_forma_pago WHERE id = :id")
    suspend fun getById(id: Int): TipoFormaPagoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TipoFormaPagoEntity)

    @Update
    suspend fun update(item: TipoFormaPagoEntity)

    @Delete
    suspend fun delete(item: TipoFormaPagoEntity)

    @Query("DELETE FROM tipos_forma_pago")
    suspend fun clearAll()
}
