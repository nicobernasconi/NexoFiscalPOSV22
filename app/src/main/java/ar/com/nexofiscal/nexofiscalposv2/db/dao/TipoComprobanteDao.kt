package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoComprobanteEntity

@Dao
interface TipoComprobanteDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM tipos_comprobante WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, TipoComprobanteEntity>

    @Query("SELECT * FROM tipos_comprobante WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, TipoComprobanteEntity>

    @Query("SELECT * FROM tipos_comprobante WHERE id = :id")
    suspend fun getById(id: Int): TipoComprobanteEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM tipos_comprobante WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<TipoComprobanteEntity>

    @Query("UPDATE tipos_comprobante SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE tipos_comprobante SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM tipos_comprobante WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM tipos_comprobante WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): TipoComprobanteEntity?

    @Transaction
    suspend fun upsertAll(tipos: List<TipoComprobanteEntity>) {
        tipos.forEach { tipo ->
            val existente = tipo.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                tipo.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                tipo
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tipo: TipoComprobanteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TipoComprobanteEntity>)

    @Update
    suspend fun update(tipo: TipoComprobanteEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM tipos_comprobante")
    suspend fun clearAll()
}