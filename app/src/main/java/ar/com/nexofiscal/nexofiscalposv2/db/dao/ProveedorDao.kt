package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProveedorEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface ProveedorDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM proveedores WHERE syncStatus != :statusDeleted ORDER BY razonSocial ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ProveedorEntity>

    @Query("SELECT * FROM proveedores WHERE (razonSocial LIKE :query OR cuit LIKE :query) AND syncStatus != :statusDeleted ORDER BY razonSocial ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ProveedorEntity>

    @Query("SELECT * FROM proveedores WHERE id = :id")
    suspend fun getById(id: Int): ProveedorEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM proveedores WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<ProveedorEntity>

    @Query("UPDATE proveedores SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE proveedores SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM proveedores WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM proveedores WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): ProveedorEntity?

    @Transaction
    suspend fun upsertAll(proveedores: List<ProveedorEntity>) {
        proveedores.forEach { proveedor ->
            val existente = proveedor.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                proveedor.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                proveedor
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(proveedor: ProveedorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ProveedorEntity>)

    @Update
    suspend fun update(proveedor: ProveedorEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM proveedores")
    suspend fun clearAll()
}