package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SubcategoriaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SubcategoriaDao {
    // --- CAMBIO: La consulta ahora excluye los registros marcados para borrar ---
    @Query("SELECT * FROM subcategorias WHERE syncStatus != :statusDeleted")
    fun getAll(statusDeleted: SyncStatus = SyncStatus.DELETED): Flow<List<SubcategoriaEntity>>

    @Query("SELECT * FROM subcategorias WHERE id = :id")
    suspend fun getById(id: Int): SubcategoriaEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM subcategorias WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<SubcategoriaEntity>

    @Query("UPDATE subcategorias SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE subcategorias SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM subcategorias WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SubcategoriaEntity)

    @Update
    suspend fun update(item: SubcategoriaEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM subcategorias")
    suspend fun clearAll()
}