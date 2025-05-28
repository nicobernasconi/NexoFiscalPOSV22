// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/ProvinciaDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProvinciaEntity

@Dao
interface ProvinciaDao {

    @Query("SELECT * FROM provincias")
    fun getAll(): Flow<List<ProvinciaEntity>>

    @Query("SELECT * FROM provincias WHERE id = :id")
    suspend fun getById(id: Int): ProvinciaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(provincia: ProvinciaEntity)

    @Update
    suspend fun update(provincia: ProvinciaEntity)

    @Delete
    suspend fun delete(provincia: ProvinciaEntity)

    @Query("DELETE FROM provincias")
    suspend fun clearAll()
}
