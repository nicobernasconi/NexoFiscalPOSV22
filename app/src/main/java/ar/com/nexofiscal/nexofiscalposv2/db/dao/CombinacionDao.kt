// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/CombinacionDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CombinacionEntity

@Dao
interface CombinacionDao {

    @Query("SELECT * FROM combinaciones")
    fun getAll(): Flow<List<CombinacionEntity>>

    @Query("SELECT * FROM combinaciones WHERE uid = :uid")
    suspend fun getByUid(uid: Int): CombinacionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comb: CombinacionEntity)

    @Update
    suspend fun update(comb: CombinacionEntity)

    @Delete
    suspend fun delete(comb: CombinacionEntity)

    @Query("DELETE FROM combinaciones")
    suspend fun clearAll()
}
