// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/LocalidadDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.LocalidadEntity

@Dao
interface LocalidadDao {

    @Query("SELECT * FROM localidades")
    fun getAll(): Flow<List<LocalidadEntity>>

    @Query("SELECT * FROM localidades WHERE id = :id")
    suspend fun getById(id: Int): LocalidadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(localidad: LocalidadEntity)

    @Update
    suspend fun update(localidad: LocalidadEntity)

    @Delete
    suspend fun delete(localidad: LocalidadEntity)

    @Query("DELETE FROM localidades")
    suspend fun clearAll()
}
