package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoIvaEntity

@Dao
interface TipoIvaDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM tipos_iva WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, TipoIvaEntity>

    @Query("SELECT * FROM tipos_iva WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, TipoIvaEntity>

    @Query("SELECT * FROM tipos_iva WHERE id = :id")
    suspend fun getById(id: Int): TipoIvaEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM tipos_iva WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<TipoIvaEntity>

    @Query("UPDATE tipos_iva SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE tipos_iva SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM tipos_iva WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM tipos_iva WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): TipoIvaEntity?

    /**
     * Inserta o actualiza una lista de tipos de IVA. Si uno ya existe
     * (mismo serverId), lo actualiza. Si no, lo inserta como nuevo.
     */
    @Transaction
    suspend fun upsertAll(tiposIva: List<TipoIvaEntity>) {
        tiposIva.forEach { tipoIva ->
            val existente = tipoIva.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                tipoIva.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                tipoIva
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tipo: TipoIvaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TipoIvaEntity>)

    @Update
    suspend fun update(tipo: TipoIvaEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM tipos_iva")
    suspend fun clearAll()

    @Query("SELECT * FROM tipos_iva WHERE syncStatus != :statusDeleted")
    suspend fun getAll(statusDeleted: SyncStatus = SyncStatus.DELETED): List<TipoIvaEntity>
}