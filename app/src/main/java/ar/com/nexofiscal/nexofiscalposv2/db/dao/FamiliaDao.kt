// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/FamiliaDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FamiliaEntity

@Dao
interface FamiliaDao {

    @Query("SELECT * FROM familias")
    fun getAll(): Flow<List<FamiliaEntity>>

    @Query("SELECT * FROM familias WHERE id = :id")
    suspend fun getById(id: Int): FamiliaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(familia: FamiliaEntity)

    @Update
    suspend fun update(familia: FamiliaEntity)

    @Delete
    suspend fun delete(familia: FamiliaEntity)

    @Query("DELETE FROM familias")
    suspend fun clearAll()
}
