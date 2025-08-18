package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.NotificacionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface NotificacionDao {
    @Query("SELECT * FROM notificaciones WHERE syncStatus != :statusDeleted ORDER BY id DESC")
    fun getPagingSource(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, NotificacionEntity>

    @Query("SELECT * FROM notificaciones WHERE (nombre LIKE :query OR mensaje LIKE :query) AND syncStatus != :statusDeleted ORDER BY id DESC")
    fun searchPagingSource(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, NotificacionEntity>

    @Query("SELECT * FROM notificaciones WHERE id = :id")
    suspend fun getById(id: Int): NotificacionEntity?

    @Query("SELECT * FROM notificaciones WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): NotificacionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificacionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<NotificacionEntity>)

    @Update
    suspend fun update(entity: NotificacionEntity)

    @Query("DELETE FROM notificaciones")
    suspend fun clearAll()

    @Transaction
    suspend fun upsertAll(items: List<NotificacionEntity>) {
        items.forEach { item ->
            val existente = item.serverId?.let { findByServerId(it) }
            val entidadParaInsertar = if (existente != null) item.copy(id = existente.id) else item
            insert(entidadParaInsertar)
        }
    }

    @Query("SELECT * FROM notificaciones WHERE (activo = 1) AND syncStatus != :statusDeleted ORDER BY tipoNotificacionId DESC, id DESC")
    suspend fun getActivas(statusDeleted: SyncStatus = SyncStatus.DELETED): List<NotificacionEntity>
}
