// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/VendedorDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.VendedorEntity

@Dao
interface VendedorDao {

    @Query("SELECT * FROM vendedores")
    fun getAll(): Flow<List<VendedorEntity>>

    @Query("SELECT * FROM vendedores WHERE id = :id")
    suspend fun getById(id: Int): VendedorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VendedorEntity)

    @Update
    suspend fun update(item: VendedorEntity)

    @Delete
    suspend fun delete(item: VendedorEntity)

    @Query("DELETE FROM vendedores")
    suspend fun clearAll()
}
