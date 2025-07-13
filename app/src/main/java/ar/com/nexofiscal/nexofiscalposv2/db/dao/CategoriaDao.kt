package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CategoriaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface CategoriaDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM categorias WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, CategoriaEntity>

    @Query("SELECT * FROM categorias WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, CategoriaEntity>

    @Query("SELECT * FROM categorias WHERE id = :id")
    suspend fun getById(id: Int): CategoriaEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM categorias WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<CategoriaEntity>

    @Query("UPDATE categorias SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE categorias SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM categorias WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM categorias WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): CategoriaEntity?

    /**
     * Inserta una lista de categorias. Si una categoría ya existe (mismo serverId),
     * la actualiza. Si no, la inserta como nueva.
     */
    @Transaction
    suspend fun upsertAll(categorias: List<CategoriaEntity>) {
        categorias.forEach { categoria ->
            val existente = categoria.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                categoria.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                categoria
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categoria: CategoriaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CategoriaEntity>)

    @Update
    suspend fun update(categoria: CategoriaEntity)

    @Query("DELETE FROM categorias")
    suspend fun clearAll()
}