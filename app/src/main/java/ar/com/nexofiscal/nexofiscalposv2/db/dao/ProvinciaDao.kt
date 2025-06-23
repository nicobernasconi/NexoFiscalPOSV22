package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProvinciaConDetalles
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProvinciaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface ProvinciaDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---

    @Transaction
    @Query("SELECT * FROM provincias WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ProvinciaConDetalles>

    @Transaction
    @Query("SELECT * FROM provincias WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ProvinciaConDetalles>


    @Query("SELECT * FROM provincias WHERE id = :id")
    suspend fun getById(id: Int): ProvinciaEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM provincias WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<ProvinciaEntity>

    @Query("UPDATE provincias SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE provincias SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM provincias WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(provincia: ProvinciaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ProvinciaEntity>)

    @Update
    suspend fun update(provincia: ProvinciaEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM provincias")
    suspend fun clearAll()
}