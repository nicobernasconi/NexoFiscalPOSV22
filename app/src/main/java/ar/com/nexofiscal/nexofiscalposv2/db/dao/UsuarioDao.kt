package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UsuarioConDetalles // --- Importar la nueva clase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UsuarioEntity

@Dao
interface UsuarioDao {
    // --- CAMBIO: El PagingSource ahora devuelve UsuarioConDetalles y usa @Transaction ---
    @Transaction
    @Query("SELECT * FROM usuarios WHERE syncStatus != :statusDeleted ORDER BY nombreCompleto ASC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, UsuarioConDetalles>

    @Transaction
    @Query("SELECT * FROM usuarios WHERE (nombreCompleto LIKE :query OR nombreUsuario LIKE :query) AND syncStatus != :statusDeleted ORDER BY nombreCompleto ASC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, UsuarioConDetalles>

    // --- El resto de los métodos permanecen igual ---
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getById(id: Int): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<UsuarioEntity>

    @Query("UPDATE usuarios SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE usuarios SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM usuarios WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: UsuarioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<UsuarioEntity>)

    @Update
    suspend fun update(usuario: UsuarioEntity)

    @Query("DELETE FROM usuarios")
    suspend fun clearAll()
}