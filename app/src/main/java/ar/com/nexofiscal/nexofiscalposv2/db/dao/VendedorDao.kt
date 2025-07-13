package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.VendedorEntity

@Dao
interface VendedorDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM vendedores WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, VendedorEntity>

    @Query("SELECT * FROM vendedores WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, VendedorEntity>

    @Query("SELECT * FROM vendedores WHERE id = :id")
    suspend fun getById(id: Int): VendedorEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM vendedores WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<VendedorEntity>

    @Query("UPDATE vendedores SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE vendedores SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM vendedores WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM vendedores WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): VendedorEntity?

    /**
     * Inserta o actualiza una lista de vendedores. Si un vendedor ya existe
     * (mismo serverId), lo actualiza. Si no, lo inserta como nuevo.
     */
    @Transaction
    suspend fun upsertAll(vendedores: List<VendedorEntity>) {
        vendedores.forEach { vendedor ->
            val existente = vendedor.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                vendedor.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                vendedor
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }


    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vendedor: VendedorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VendedorEntity>)

    @Update
    suspend fun update(vendedor: VendedorEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM vendedores")
    suspend fun clearAll()
}