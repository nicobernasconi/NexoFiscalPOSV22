package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobanteConDetallesEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface ComprobanteDao {

        @Transaction
        // --- INICIO DE LA MODIFICACIÓN 1 ---
        @Query("SELECT * FROM comprobantes WHERE syncStatus != :statusDeleted ORDER BY fecha DESC, hora DESC")
        fun getPagingSourceWithDetails(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ComprobanteConDetallesEntity>
        // --- FIN DE LA MODIFICACIÓN 1 ---

        @Transaction
        @Query("""
        SELECT comprobantes.* FROM comprobantes 
        LEFT JOIN clientes ON comprobantes.clienteId = clientes.serverId 
        WHERE (clientes.nombre LIKE :query OR CAST(comprobantes.numeroFactura AS TEXT) LIKE :query OR CAST(comprobantes.numero AS TEXT) LIKE :query)
        AND comprobantes.syncStatus != :statusDeleted
        ORDER BY comprobantes.fecha DESC, comprobantes.hora DESC
    """) // --- MODIFICACIÓN 2: Se añade 'comprobantes.hora DESC' ---
        fun searchPagingSourceWithDetails(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ComprobanteConDetallesEntity>

        @Query("SELECT * FROM comprobantes WHERE id = :id")
        suspend fun getById(id: Int): ComprobanteEntity?

        @Query("SELECT MAX(numero) FROM comprobantes WHERE tipoComprobanteId = :tipoId")
        suspend fun getHighestNumeroForTipo(tipoId: Int): Int?

        @Query("SELECT * FROM comprobantes WHERE syncStatus != :statusSynced")
        suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<ComprobanteEntity>

        @Query("UPDATE comprobantes SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
        suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

        @Query("DELETE FROM comprobantes WHERE id = :localId")
        suspend fun deleteByLocalId(localId: Int)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
         fun insert(comprobante: ComprobanteEntity): Long

        @Update
        suspend fun update(comprobante: ComprobanteEntity)

        @Query("DELETE FROM comprobantes")
        suspend fun clearAll()
}