package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FormaPagoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface FormaPagoDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM formas_pago WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, FormaPagoEntity>

    @Query("SELECT * FROM formas_pago WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, FormaPagoEntity>

    @Query("SELECT * FROM formas_pago WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getAll(statusDeleted: SyncStatus = SyncStatus.DELETED): Flow<List<FormaPagoEntity>>

    @Query("SELECT * FROM formas_pago WHERE id = :id")
    suspend fun getById(id: Int): FormaPagoEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM formas_pago WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<FormaPagoEntity>

    @Query("UPDATE formas_pago SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE formas_pago SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM formas_pago WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(forma: FormaPagoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FormaPagoEntity>)

    @Update
    suspend fun update(forma: FormaPagoEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM formas_pago")
    suspend fun clearAll()
}