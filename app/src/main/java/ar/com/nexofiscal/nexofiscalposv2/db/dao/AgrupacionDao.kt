// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/AgrupacionDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.AgrupacionEntity

@Dao
interface AgrupacionDao {

    @Query("SELECT * FROM agrupaciones")
    fun getAll(): Flow<List<AgrupacionEntity>>

    @Query("SELECT * FROM agrupaciones WHERE id = :id")
    suspend fun getById(id: Int): AgrupacionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(agrupacion: AgrupacionEntity)

    @Update
    suspend fun update(agrupacion: AgrupacionEntity)

    @Delete
    suspend fun delete(agrupacion: AgrupacionEntity)

    @Query("DELETE FROM agrupaciones")
    suspend fun clearAll()
}
