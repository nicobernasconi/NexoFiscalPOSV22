// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/CategoriaDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CategoriaEntity

@Dao
interface CategoriaDao {

    @Query("SELECT * FROM categorias")
    fun getAll(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias WHERE id = :id")
    suspend fun getById(id: Int): CategoriaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categoria: CategoriaEntity)

    @Update
    suspend fun update(categoria: CategoriaEntity)

    @Delete
    suspend fun delete(categoria: CategoriaEntity)

    @Query("DELETE FROM categorias")
    suspend fun clearAll()
}
