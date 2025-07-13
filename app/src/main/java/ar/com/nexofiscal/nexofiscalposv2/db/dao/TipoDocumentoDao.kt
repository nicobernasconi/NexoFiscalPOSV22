package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoDocumentoEntity

@Dao
interface TipoDocumentoDao {
    @Query("SELECT * FROM tipos_documento WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, TipoDocumentoEntity>

    @Query("SELECT * FROM tipos_documento WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, TipoDocumentoEntity>

    @Query("SELECT * FROM tipos_documento WHERE id = :id")
    suspend fun getById(id: Int): TipoDocumentoEntity?

    @Query("SELECT * FROM tipos_documento WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<TipoDocumentoEntity>

    @Query("UPDATE tipos_documento SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE tipos_documento SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM tipos_documento WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tipo: TipoDocumentoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TipoDocumentoEntity>)

    @Update
    suspend fun update(tipo: TipoDocumentoEntity)

    @Query("DELETE FROM tipos_documento")
    suspend fun clearAll()

    // --- FUNCIONES CORREGIDAS/AÑADIDAS ---
    @Query("SELECT * FROM tipos_documento WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): TipoDocumentoEntity?

    @Transaction
    suspend fun upsertAll(tipos: List<TipoDocumentoEntity>) {
        tipos.forEach { tipo ->
            val existente = tipo.serverId?.let { findByServerId(it) }
            val entidadParaInsertar = if (existente != null) {
                tipo.copy(id = existente.id)
            } else {
                tipo
            }
            insert(entidadParaInsertar)
        }
    }
}