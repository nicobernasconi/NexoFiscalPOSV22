// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/TipoIvaDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoIvaEntity

@Dao
interface TipoIvaDao {

    @Query("SELECT * FROM tipos_iva")
    fun getAll(): Flow<List<TipoIvaEntity>>

    @Query("SELECT * FROM tipos_iva WHERE id = :id")
    suspend fun getById(id: Int): TipoIvaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TipoIvaEntity)

    @Update
    suspend fun update(item: TipoIvaEntity)

    @Delete
    suspend fun delete(item: TipoIvaEntity)

    @Query("DELETE FROM tipos_iva")
    suspend fun clearAll()
}
