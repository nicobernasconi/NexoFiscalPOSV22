package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface StockProductoDao {

    // --- CAMBIO: La consulta ahora excluye los registros marcados para borrar ---
    @Query("SELECT * FROM stock_productos WHERE syncStatus != :statusDeleted")
    fun getAll(statusDeleted: SyncStatus = SyncStatus.DELETED): Flow<List<StockProductoEntity>>

    @Query("SELECT * FROM stock_productos WHERE id = :id")
    suspend fun getById(id: Int): StockProductoEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM stock_productos WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<StockProductoEntity>

    @Query("UPDATE stock_productos SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE stock_productos SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM stock_productos WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StockProductoEntity)

    @Update
    suspend fun update(item: StockProductoEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM stock_productos")
    suspend fun clearAll()
}