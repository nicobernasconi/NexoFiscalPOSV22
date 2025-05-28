// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/PaisDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PaisEntity

@Dao
interface PaisDao {

    @Query("SELECT * FROM paises")
    fun getAll(): Flow<List<PaisEntity>>

    @Query("SELECT * FROM paises WHERE id = :id")
    suspend fun getById(id: Int): PaisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pais: PaisEntity)

    @Update
    suspend fun update(pais: PaisEntity)

    @Delete
    suspend fun delete(pais: PaisEntity)

    @Query("DELETE FROM paises")
    suspend fun clearAll()
}
