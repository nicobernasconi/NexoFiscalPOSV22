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

    @Query("SELECT * FROM usuarios WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): UsuarioEntity?

    /**
     * Inserta o actualiza una lista de usuarios. Si un usuario ya existe
     * (mismo serverId), lo actualiza. Si no, lo inserta como nuevo.
     */
    @Transaction
    suspend fun upsertAll(usuarios: List<UsuarioEntity>) {
        usuarios.forEach { usuario ->
            val existente = usuario.serverId?.let { findByServerId(it) }

            val entidadParaInsertar = if (existente != null) {
                // Si existe, se copia la nueva info pero se mantiene el ID local
                usuario.copy(id = existente.id)
            } else {
                // Si no existe, es un registro nuevo
                usuario
            }

            insert(entidadParaInsertar) // El método insert ya tiene OnConflictStrategy.REPLACE
        }
    }

    @Transaction
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getConDetallesById(id: Int): UsuarioConDetalles?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: UsuarioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<UsuarioEntity>)

    @Update
    suspend fun update(usuario: UsuarioEntity)

    @Query("DELETE FROM usuarios")
    suspend fun clearAll()
}