package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.LocalidadEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface LocalidadDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM localidades WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, LocalidadEntity>

    @Query("SELECT * FROM localidades WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, LocalidadEntity>

    @Query("SELECT * FROM localidades WHERE id = :id")
    suspend fun getById(id: Int): LocalidadEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM localidades WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<LocalidadEntity>

    @Query("UPDATE localidades SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE localidades SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM localidades WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM localidades WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): LocalidadEntity?

    /**
     * Inserta o actualiza una lista de localidades. Si una localidad ya existe
     * (mismo serverId), la actualiza. Si no, la inserta como nueva.
     */
    @Transaction
    suspend fun upsertAll(localidades: List<LocalidadEntity>) {
        localidades.forEach { localidad ->
            val existente = localidad.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                localidad.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                localidad
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(localidad: LocalidadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<LocalidadEntity>)

    @Update
    suspend fun update(localidad: LocalidadEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM localidades")
    suspend fun clearAll()
}