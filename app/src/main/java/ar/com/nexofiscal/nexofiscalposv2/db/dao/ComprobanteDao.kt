package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import androidx.room.SkipQueryVerification
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobanteConDetalles
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface ComprobanteDao {
        @Transaction
        @Query("SELECT * FROM comprobantes WHERE syncStatus != :statusDeleted ORDER BY fecha DESC, hora DESC")
        fun getPagingSourceWithDetails(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ComprobanteConDetalles>
        @Transaction
        @Query("""
        SELECT comprobantes.* FROM comprobantes 
        LEFT JOIN clientes ON comprobantes.clienteId = clientes.serverId 
        WHERE (clientes.nombre LIKE :query OR CAST(comprobantes.numeroFactura AS TEXT) LIKE :query OR CAST(comprobantes.numero AS TEXT) LIKE :query)
        AND comprobantes.syncStatus != :statusDeleted
        ORDER BY comprobantes.fecha DESC, comprobantes.hora DESC
    """)
        fun searchPagingSourceWithDetails(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ComprobanteConDetalles>

        @Query("SELECT * FROM comprobantes WHERE id = :id")
        suspend fun getById(id: Int): ComprobanteEntity?

        @Query("SELECT * FROM comprobantes")
        suspend fun getAll(): List<ComprobanteEntity>

        @Query("SELECT MAX(numero) FROM comprobantes WHERE tipoComprobanteId = :tipoId")
        suspend fun getHighestNumeroForTipo(tipoId: Int): Int?

        @Query("SELECT * FROM comprobantes WHERE serverId = :serverId LIMIT 1")
        fun getByServerId(serverId: Int): ComprobanteEntity?

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

        // --- INICIO DEL CÓDIGO AÑADIDO Y CORREGIDO ---
        @Transaction
        @Query("""
        SELECT * FROM comprobantes
        WHERE
            fechaBaja IS NULL
            AND (:fechaDesde IS NULL OR fecha >= :fechaDesde)
            AND (:fechaHasta IS NULL OR fecha <= :fechaHasta)
            AND (tipoComprobanteId IN (:tipoComprobanteIds))
            AND (:clienteId IS NULL OR clienteId = :clienteId)
            AND (:vendedorId IS NULL OR vendedorId = :vendedorId)
            AND (:usuarioId IS NULL OR usuarioId = :usuarioId)
        ORDER BY fecha DESC, hora DESC
    """)
        suspend fun getComprobantesParaInforme(
                fechaDesde: String?,
                fechaHasta: String?,
                tipoComprobanteIds: List<Int>,
                clienteId: Int?,
                vendedorId: Int?,
                usuarioId: Int?
        ): List<ComprobanteConDetalles>

        @Query("DELETE FROM comprobante_pagos WHERE comprobanteLocalId = :comprobanteId")
        suspend fun deletePagosForComprobante(comprobanteId: Int)

        @Query("DELETE FROM comprobante_promociones WHERE comprobanteLocalId = :comprobanteId")
        suspend fun deletePromocionesForComprobante(comprobanteId: Int)

        // --- NUEVO: Asignar cierre a comprobantes del usuario que no tengan cierre y no estén dados de baja ---
        @Query("""
            UPDATE comprobantes
            SET cierreCajaId = :cierreId
            WHERE cierreCajaId IS NULL
              AND (fechaBaja IS NULL OR fechaBaja = '')
              AND usuarioId = :usuarioId
        """)
        @SkipQueryVerification
        suspend fun asignarCierreAComprobantesDeUsuario(usuarioId: Int, cierreId: Int): Int

        // Métricas útiles para el cierre (opcional)
        @Query("SELECT COUNT(*) FROM comprobantes WHERE cierreCajaId IS NULL AND (fechaBaja IS NULL OR fechaBaja = '') AND usuarioId = :usuarioId")
        @SkipQueryVerification
        suspend fun contarComprobantesPendientesDeCierre(usuarioId: Int): Int

        @Query("SELECT IFNULL(SUM(CAST(total AS REAL)),0) FROM comprobantes WHERE cierreCajaId IS NULL AND (fechaBaja IS NULL OR fechaBaja = '') AND usuarioId = :usuarioId")
        @SkipQueryVerification
        suspend fun sumarTotalPendienteDeCierre(usuarioId: Int): Double

        // --- NUEVO: Obtener comprobantes (con pagos) por cierre de caja ---
        @Transaction
        @Query("SELECT * FROM comprobantes WHERE cierreCajaId = :cierreId")
        suspend fun getComprobantesPorCierre(cierreId: Int): List<ComprobanteConDetalles>
}