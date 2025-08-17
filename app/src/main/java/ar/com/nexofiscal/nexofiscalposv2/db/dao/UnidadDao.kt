package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UnidadEntity

@Dao
interface UnidadDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM unidades WHERE syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, UnidadEntity>

    @Query("SELECT * FROM unidades WHERE nombre LIKE :query AND syncStatus != :statusDeleted ORDER BY nombre ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, UnidadEntity>

    @Query("SELECT * FROM unidades WHERE id = :id")
    suspend fun getById(id: Int): UnidadEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM unidades WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<UnidadEntity>

    @Query("UPDATE unidades SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE unidades SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM unidades WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)


    @Query("SELECT * FROM unidades WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): UnidadEntity?

    /**
     * Inserta o actualiza una lista de unidades. Si una unidad ya existe
     * (mismo serverId), la actualiza. Si no, la inserta como nueva.
     */
    @Transaction
    suspend fun upsertAll(unidades: List<UnidadEntity>) {
        unidades.forEach { unidad ->
            val existente = unidad.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                unidad.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                unidad
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }


    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(unidad: UnidadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<UnidadEntity>)

    @Update
    suspend fun update(unidad: UnidadEntity)

    // El @Delete original se elimina, el borrado ahora es lógico.

    @Query("DELETE FROM unidades")
    suspend fun clearAll()

    // Conteo de productos que referencian esta unidad (productos.unidadId -> unidades.serverId)
    @Query("SELECT COUNT(*) FROM productos WHERE unidadId = :unidadServerId AND syncStatus != :statusDeleted")
    suspend fun countProductosReferencingUnidad(unidadServerId: Int, statusDeleted: SyncStatus = SyncStatus.DELETED): Int
}