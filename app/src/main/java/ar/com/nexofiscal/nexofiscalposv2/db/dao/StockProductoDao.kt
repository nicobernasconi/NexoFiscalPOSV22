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

    // Nuevo método para obtener stock por producto y sucursal
    @Query("SELECT * FROM stock_productos WHERE productoId = :productoId AND sucursalId = :sucursalId LIMIT 1")
    suspend fun getByProductoId(productoId: Int, sucursalId: Int): StockProductoEntity?

    // Método para obtener stock solo por producto (primera sucursal encontrada)
    @Query("SELECT * FROM stock_productos WHERE productoId = :productoId LIMIT 1")
    suspend fun getByProductoId(productoId: Int): StockProductoEntity?

    @Transaction
    suspend fun insertOrUpdate(items: List<StockProductoEntity>) {
        for (item in items) {
            val existingItem = getById(item.id)
            if (existingItem == null) {
                insert(item)
            } else {
                update(item)
            }
        }
    }

    @Query("SELECT * FROM stock_productos WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): StockProductoEntity?

    /**
     * Inserta o actualiza una lista de registros de stock. Si un registro ya existe
     * (mismo serverId), lo actualiza. Si no, lo inserta como nuevo.
     */
    @Transaction
    suspend fun upsertAll(items: List<StockProductoEntity>) {
        items.forEach { item ->
            val existente = item.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                item.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                item
            }
            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }
}