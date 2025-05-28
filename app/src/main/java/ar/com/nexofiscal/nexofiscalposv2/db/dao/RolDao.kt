// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/RolDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RolEntity

@Dao
interface RolDao {

    @Query("SELECT * FROM roles")
    fun getAll(): Flow<List<RolEntity>>

    @Query("SELECT * FROM roles WHERE id = :id")
    suspend fun getById(id: Int): RolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rol: RolEntity)

    @Update
    suspend fun update(rol: RolEntity)

    @Delete
    suspend fun delete(rol: RolEntity)

    @Query("DELETE FROM roles")
    suspend fun clearAll()
}
