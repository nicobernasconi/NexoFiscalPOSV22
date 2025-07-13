package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TasaIvaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface TasaIvaDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM tasas_iva WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, TasaIvaEntity>

    @Query("SELECT * FROM tasas_iva WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, TasaIvaEntity>

    @Query("SELECT * FROM tasas_iva WHERE id = :id")
    suspend fun getById(id: Int): TasaIvaEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM tasas_iva WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<TasaIvaEntity>

    @Query("UPDATE tasas_iva SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE tasas_iva SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM tasas_iva WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM tasas_iva WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): TasaIvaEntity?

    /**
     * Inserta o actualiza una lista de tasas de IVA. Si una ya existe
     * (mismo serverId), la actualiza. Si no, la inserta como nueva.
     */
    @Transaction
    suspend fun upsertAll(tasas: List<TasaIvaEntity>) {
        tasas.forEach { tasa ->
            val existente = tasa.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                tasa.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                tasa
            }

            insert(entidadParaInsertar)
        }
    }

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tasa: TasaIvaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TasaIvaEntity>)

    @Update
    suspend fun update(tasa: TasaIvaEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM tasas_iva")
    suspend fun clearAll()
}