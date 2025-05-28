// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/TipoDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoEntity

@Dao
interface TipoDao {
    @Query("SELECT * FROM tipos")
    fun getAll(): Flow<List<TipoEntity>>

    @Query("SELECT * FROM tipos WHERE id = :id")
    suspend fun getById(id: Int): TipoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TipoEntity)

    @Update
    suspend fun update(item: TipoEntity)

    @Delete
    suspend fun delete(item: TipoEntity)

    @Query("DELETE FROM tipos")
    suspend fun clearAll()
}
