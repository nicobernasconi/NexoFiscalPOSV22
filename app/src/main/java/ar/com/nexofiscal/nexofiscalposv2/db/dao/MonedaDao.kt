// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/MonedaDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.MonedaEntity

@Dao
interface MonedaDao {

    @Query("SELECT * FROM monedas")
    fun getAll(): Flow<List<MonedaEntity>>

    @Query("SELECT * FROM monedas WHERE id = :id")
    suspend fun getById(id: Int): MonedaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(moneda: MonedaEntity)

    @Update
    suspend fun update(moneda: MonedaEntity)

    @Delete
    suspend fun delete(moneda: MonedaEntity)

    @Query("DELETE FROM monedas")
    suspend fun clearAll()
}
