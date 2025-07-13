package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ClienteConDetalles
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ClienteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface ClienteDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM clientes WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ClienteEntity>

    @Query("SELECT * FROM clientes WHERE (nombre LIKE :query OR cuit LIKE :query) AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ClienteEntity>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun getById(id: Int): ClienteEntity?

    @Query("SELECT * FROM clientes WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Int): ClienteEntity?

    @Transaction
    @Query("SELECT * FROM clientes WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSourceWithDetails(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ClienteConDetalles>

    @Transaction
    @Query("SELECT * FROM clientes WHERE (nombre LIKE :query OR cuit LIKE :query) AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSourceWithDetails(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ClienteConDetalles>

    // Añade este nuevo método para obtener una entidad única con todos sus detalles
    @Transaction
    @Query("SELECT * FROM clientes WHERE id = :localId")
    suspend fun getConDetallesById(localId: Int): ClienteConDetalles?


    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM clientes WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<ClienteEntity>

    @Query("UPDATE clientes SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE clientes SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM clientes WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)


    @Transaction
    suspend fun upsert(cliente: ClienteEntity) {
        // Busca si ya existe un cliente con el mismo serverId.
        val existente = cliente.serverId?.let { getByServerId(it) }

        if (existente != null) {
            // Si existe, actualiza el registro existente preservando su ID local autogenerado.
            // Se usa el método `update` de Room.
            update(cliente.copy(id = existente.id))
        } else {
            // Si no existe, inserta un nuevo registro.
            insert(cliente)
        }
    }

    @Transaction
    suspend fun upsertAll(clientes: List<ClienteEntity>) {
        clientes.forEach { upsert(it) }
    }

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cliente: ClienteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ClienteEntity>)

    @Update
    suspend fun update(cliente: ClienteEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM clientes")
    suspend fun clearAll()
}