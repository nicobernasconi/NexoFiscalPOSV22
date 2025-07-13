package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SucursalEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface SucursalDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM sucursales WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, SucursalEntity>

    @Query("SELECT * FROM sucursales WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, SucursalEntity>

    @Query("SELECT * FROM sucursales WHERE id = :id")
    suspend fun getById(id: Int): SucursalEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM sucursales WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<SucursalEntity>

    @Query("UPDATE sucursales SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE sucursales SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM sucursales WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM sucursales WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): SucursalEntity?

    /**
     * Inserta o actualiza una lista de sucursales. Si una sucursal ya existe
     * (mismo serverId), la actualiza. Si no, la inserta como nueva.
     */
    @Transaction
    suspend fun upsertAll(sucursales: List<SucursalEntity>) {
        sucursales.forEach { sucursal ->
            val existente = sucursal.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                sucursal.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                sucursal
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }


    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sucursal: SucursalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SucursalEntity>)

    @Update
    suspend fun update(sucursal: SucursalEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM sucursales")
    suspend fun clearAll()
}