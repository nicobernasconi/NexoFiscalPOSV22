package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CierreCajaResumenView

@Dao
interface CierreCajaDao {

    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Query("SELECT * FROM cierres_caja WHERE syncStatus != :statusDeleted ORDER BY fecha DESC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, CierreCajaEntity>

    @Query("SELECT * FROM cierres_caja WHERE fecha LIKE :query AND syncStatus != :statusDeleted ORDER BY fecha DESC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, CierreCajaEntity>

    @Query("SELECT * FROM cierres_caja WHERE id = :id")
    suspend fun getById(id: Int): CierreCajaEntity?

    // --- NUEVO: vista de resumen de cierres de caja ---
    @Query("SELECT * FROM vw_cierres_caja ORDER BY id DESC")
    suspend fun getResumen(): List<CierreCajaResumenView>

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM cierres_caja WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<CierreCajaEntity>

    @Query("UPDATE cierres_caja SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE cierres_caja SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM cierres_caja WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("SELECT * FROM cierres_caja WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): CierreCajaEntity?

    /**
     * Inserta o actualiza una lista de cierres de caja.
     */
    @Transaction
    suspend fun upsertAll(cierres: List<CierreCajaEntity>) {
        cierres.forEach { cierre ->
            val existente = cierre.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                cierre.copy(id = existente.id)
            } else {
                cierre
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cierre: CierreCajaEntity): Long

    @Update
    suspend fun update(cierre: CierreCajaEntity)

    // El @Delete original se elimina, el borrado ahora es lógico (cambio de estado).

    @Query("DELETE FROM cierres_caja")
    suspend fun clearAll()
}