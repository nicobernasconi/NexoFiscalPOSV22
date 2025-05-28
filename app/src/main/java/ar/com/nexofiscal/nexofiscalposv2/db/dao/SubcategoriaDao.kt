// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/SubcategoriaDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SubcategoriaEntity

@Dao
interface SubcategoriaDao {

    @Query("SELECT * FROM subcategorias")
    fun getAll(): Flow<List<SubcategoriaEntity>>

    @Query("SELECT * FROM subcategorias WHERE id = :id")
    suspend fun getById(id: Int): SubcategoriaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SubcategoriaEntity)

    @Update
    suspend fun update(item: SubcategoriaEntity)

    @Delete
    suspend fun delete(item: SubcategoriaEntity)

    @Query("DELETE FROM subcategorias")
    suspend fun clearAll()
}
