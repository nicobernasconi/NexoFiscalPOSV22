// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/CierreCajaDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity

@Dao
interface CierreCajaDao {

    @Query("SELECT * FROM cierres_caja")
    fun getAll(): Flow<List<CierreCajaEntity>>

    @Query("SELECT * FROM cierres_caja WHERE id = :id")
    suspend fun getById(id: Int): CierreCajaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cierre: CierreCajaEntity)

    @Update
    suspend fun update(cierre: CierreCajaEntity)

    @Delete
    suspend fun delete(cierre: CierreCajaEntity)

    @Query("DELETE FROM cierres_caja")
    suspend fun clearAll()
}
