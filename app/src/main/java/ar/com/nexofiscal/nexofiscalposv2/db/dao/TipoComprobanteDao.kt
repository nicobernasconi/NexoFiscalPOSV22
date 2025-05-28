// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/TipoComprobanteDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoComprobanteEntity

@Dao
interface TipoComprobanteDao {

    @Query("SELECT * FROM tipos_comprobante")
    fun getAll(): Flow<List<TipoComprobanteEntity>>

    @Query("SELECT * FROM tipos_comprobante WHERE id = :id")
    suspend fun getById(id: Int): TipoComprobanteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TipoComprobanteEntity)

    @Update
    suspend fun update(item: TipoComprobanteEntity)

    @Delete
    suspend fun delete(item: TipoComprobanteEntity)

    @Query("DELETE FROM tipos_comprobante")
    suspend fun clearAll()
}
