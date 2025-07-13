package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RolEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface RolDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM roles WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, RolEntity>

    @Query("SELECT * FROM roles WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, RolEntity>

    @Query("SELECT * FROM roles WHERE id = :id")
    suspend fun getById(id: Int): RolEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM roles WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<RolEntity>

    @Query("UPDATE roles SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE roles SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM roles WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM roles WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): RolEntity?

    /**
     * Inserta o actualiza una lista de roles. Si un rol ya existe
     * (mismo serverId), lo actualiza. Si no, lo inserta como nuevo.
     */
    @Transaction
    suspend fun upsertAll(roles: List<RolEntity>) {
        roles.forEach { rol ->
            val existente = rol.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                rol.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                rol
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }


    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rol: RolEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RolEntity>)

    @Update
    suspend fun update(rol: RolEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM roles")
    suspend fun clearAll()
}