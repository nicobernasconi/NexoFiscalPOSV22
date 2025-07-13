package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.AgrupacionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface AgrupacionDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados como borrados ---
    @Query("SELECT * FROM agrupaciones WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, AgrupacionEntity>

    @Query("SELECT * FROM agrupaciones WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, AgrupacionEntity>

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM agrupaciones WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<AgrupacionEntity>

    @Query("UPDATE agrupaciones SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE agrupaciones SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM agrupaciones WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM agrupaciones WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): AgrupacionEntity?

    /**
     * Inserta una lista de agrupaciones. Si una agrupación ya existe (mismo serverId),
     * la actualiza. Si no, la inserta como nueva.
     */
    @Transaction
    suspend fun upsertAll(agrupaciones: List<AgrupacionEntity>) {
        agrupaciones.forEach { agrupacion ->
            val existente = agrupacion.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                agrupacion.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                agrupacion
            }

            insert(entidadParaInsertar)
        }
    }

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Query("SELECT * FROM agrupaciones WHERE id = :id")
    suspend fun getById(id: Int): AgrupacionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(agrupacion: AgrupacionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AgrupacionEntity>)

    @Update
    suspend fun update(agrupacion: AgrupacionEntity)

    // El @Delete original se elimina, el borrado ahora es lógico (cambio de estado).

    @Query("DELETE FROM agrupaciones")
    suspend fun clearAll()
}