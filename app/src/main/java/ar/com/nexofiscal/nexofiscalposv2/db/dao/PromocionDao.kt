// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/PromocionDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PromocionEntity

@Dao
interface PromocionDao {

    @Query("SELECT * FROM promociones")
    fun getAll(): Flow<List<PromocionEntity>>

    @Query("SELECT * FROM promociones WHERE id = :id")
    suspend fun getById(id: Int): PromocionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(promocion: PromocionEntity)

    @Update
    suspend fun update(promocion: PromocionEntity)

    @Delete
    suspend fun delete(promocion: PromocionEntity)

    @Query("DELETE FROM promociones")
    suspend fun clearAll()
}
