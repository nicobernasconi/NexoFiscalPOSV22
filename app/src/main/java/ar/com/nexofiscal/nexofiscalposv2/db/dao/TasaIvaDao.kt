// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/TasaIvaDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TasaIvaEntity

@Dao
interface TasaIvaDao {

    @Query("SELECT * FROM tasa_iva")
    fun getAll(): Flow<List<TasaIvaEntity>>

    @Query("SELECT * FROM tasa_iva WHERE id = :id")
    suspend fun getById(id: Int): TasaIvaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TasaIvaEntity)

    @Update
    suspend fun update(item: TasaIvaEntity)

    @Delete
    suspend fun delete(item: TasaIvaEntity)

    @Query("DELETE FROM tasa_iva")
    suspend fun clearAll()
}
