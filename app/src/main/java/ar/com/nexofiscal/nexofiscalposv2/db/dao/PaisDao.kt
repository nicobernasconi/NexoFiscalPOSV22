package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PaisEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface PaisDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM paises WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, PaisEntity>

    @Query("SELECT * FROM paises WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, PaisEntity>

    @Query("SELECT * FROM paises WHERE id = :id")
    suspend fun getById(id: Int): PaisEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM paises WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<PaisEntity>

    @Query("UPDATE paises SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE paises SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM paises WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM paises WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): PaisEntity?

    /**
     * Inserta o actualiza una lista de paises. Si un país ya existe
     * (mismo serverId), lo actualiza. Si no, lo inserta como nuevo.
     */
    @Transaction
    suspend fun upsertAll(paises: List<PaisEntity>) {
        paises.forEach { pais ->
            val existente = pais.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                pais.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                pais
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }


    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pais: PaisEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PaisEntity>)

    @Update
    suspend fun update(pais: PaisEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM paises")
    suspend fun clearAll()
}