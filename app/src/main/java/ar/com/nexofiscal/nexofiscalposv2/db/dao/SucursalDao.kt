// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/SucursalDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SucursalEntity

@Dao
interface SucursalDao {

    @Query("SELECT * FROM sucursales")
    fun getAll(): Flow<List<SucursalEntity>>

    @Query("SELECT * FROM sucursales WHERE id = :id")
    suspend fun getById(id: Int): SucursalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sucursal: SucursalEntity)

    @Update
    suspend fun update(sucursal: SucursalEntity)

    @Delete
    suspend fun delete(sucursal: SucursalEntity)

    @Query("DELETE FROM sucursales")
    suspend fun clearAll()
}
