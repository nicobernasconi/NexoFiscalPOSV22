// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/ProveedorDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProveedorEntity

@Dao
interface ProveedorDao {

    @Query("SELECT * FROM proveedores")
    fun getAll(): Flow<List<ProveedorEntity>>

    @Query("SELECT * FROM proveedores WHERE id = :id")
    suspend fun getById(id: Int): ProveedorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(proveedor: ProveedorEntity)

    @Update
    suspend fun update(proveedor: ProveedorEntity)

    @Delete
    suspend fun delete(proveedor: ProveedorEntity)

    @Query("DELETE FROM proveedores")
    suspend fun clearAll()
}
