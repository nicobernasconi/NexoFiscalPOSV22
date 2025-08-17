package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.MonedaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface MonedaDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM monedas WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, MonedaEntity>

    @Query("SELECT * FROM monedas WHERE (nombre LIKE :query OR simbolo LIKE :query) AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, MonedaEntity>

    @Query("SELECT * FROM monedas WHERE id = :id")
    suspend fun getById(id: Int): MonedaEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM monedas WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<MonedaEntity>

    @Query("UPDATE monedas SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE monedas SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM monedas WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM monedas WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): MonedaEntity?

    /**
     * Inserta o actualiza una lista de monedas. Si una moneda ya existe
     * (mismo serverId), la actualiza. Si no, la inserta como nueva.
     */
    @Transaction
    suspend fun upsertAll(monedas: List<MonedaEntity>) {
        monedas.forEach { moneda ->
            val existente = moneda.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                moneda.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                moneda
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }


    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(moneda: MonedaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MonedaEntity>)

    @Update
    suspend fun update(moneda: MonedaEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM monedas")
    suspend fun clearAll()

    // Conteo de productos que referencian esta moneda (productos.monedaId -> monedas.serverId)
    @Query("SELECT COUNT(*) FROM productos WHERE monedaId = :monedaServerId AND syncStatus != :statusDeleted")
    suspend fun countProductosReferencingMoneda(monedaServerId: Int, statusDeleted: SyncStatus = SyncStatus.DELETED): Int
}