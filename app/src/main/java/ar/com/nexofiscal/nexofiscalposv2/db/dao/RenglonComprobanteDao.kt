// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/RenglonComprobanteDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RenglonComprobanteEntity

@Dao
interface RenglonComprobanteDao {

    @Query("SELECT * FROM renglones_comprobante")
    fun getAll(): Flow<List<RenglonComprobanteEntity>>

    @Query("SELECT * FROM renglones_comprobante WHERE id = :id")
    suspend fun getById(id: Int): RenglonComprobanteEntity?

    @Query("SELECT * FROM renglones_comprobante WHERE comprobanteId = :compId")
    fun getByComprobante(compId: Int): Flow<List<RenglonComprobanteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RenglonComprobanteEntity)

    @Update
    suspend fun update(entity: RenglonComprobanteEntity)

    @Delete
    suspend fun delete(entity: RenglonComprobanteEntity)

    @Query("DELETE FROM renglones_comprobante")
    suspend fun clearAll()
}
