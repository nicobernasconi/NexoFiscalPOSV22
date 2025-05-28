// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/UnidadDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UnidadEntity

@Dao
interface UnidadDao {

    @Query("SELECT * FROM unidades")
    fun getAll(): Flow<List<UnidadEntity>>

    @Query("SELECT * FROM unidades WHERE id = :id")
    suspend fun getById(id: Int): UnidadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: UnidadEntity)

    @Update
    suspend fun update(item: UnidadEntity)

    @Delete
    suspend fun delete(item: UnidadEntity)

    @Query("DELETE FROM unidades")
    suspend fun clearAll()
}
