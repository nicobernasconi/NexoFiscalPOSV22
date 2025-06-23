package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RenglonComprobanteEntity

@Dao
interface RenglonComprobanteDao {
    // ... (métodos existentes sin cambios)
    @Query("SELECT * FROM renglones_comprobante")
    fun getAll(): Flow<List<RenglonComprobanteEntity>>

    @Query("SELECT * FROM renglones_comprobante WHERE id = :id")
    suspend fun getById(id: Int): RenglonComprobanteEntity?

    @Query("SELECT * FROM renglones_comprobante WHERE comprobanteLocalId = :localId")
    fun getByComprobante(localId: Int): Flow<List<RenglonComprobanteEntity>>

    // --- CAMBIO: Se añade un método para borrar todos los renglones de un comprobante ---
    @Query("DELETE FROM renglones_comprobante WHERE comprobanteLocalId = :compId")
    suspend fun deleteByComprobanteId(compId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RenglonComprobanteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertAll(entities: List<RenglonComprobanteEntity>)

    @Update
    suspend fun update(entity: RenglonComprobanteEntity)

    @Delete
    suspend fun delete(entity: RenglonComprobanteEntity)

    @Query("DELETE FROM renglones_comprobante")
    suspend fun clearAll()
}