package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.GastoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

@Dao
interface GastoDao {
    @Query("SELECT * FROM gastos WHERE id = :id")
    suspend fun getById(id: Int): GastoEntity?

    @Query("SELECT * FROM gastos WHERE syncStatus != :statusDeleted ORDER BY fecha DESC, id DESC")
    suspend fun getAll(statusDeleted: SyncStatus = SyncStatus.DELETED): List<GastoEntity>

    @Query("SELECT * FROM gastos WHERE cierreCajaId = :cierreId AND syncStatus != :statusDeleted ORDER BY fecha DESC, id DESC")
    suspend fun getByCierre(cierreId: Int, statusDeleted: SyncStatus = SyncStatus.DELETED): List<GastoEntity>

    @Query("SELECT IFNULL(SUM(monto),0) FROM gastos WHERE cierreCajaId = :cierreId AND syncStatus != :statusDeleted")
    suspend fun getTotalPorCierre(cierreId: Int, statusDeleted: SyncStatus = SyncStatus.DELETED): Double

    @Query(
        "SELECT * FROM gastos WHERE fecha BETWEEN :desde AND :hasta AND syncStatus != :statusDeleted ORDER BY fecha DESC, id DESC"
    )
    suspend fun getByDateRange(
        desde: String,
        hasta: String,
        statusDeleted: SyncStatus = SyncStatus.DELETED
    ): List<GastoEntity>

    @Query(
        "SELECT * FROM gastos WHERE fecha BETWEEN :desde AND :hasta AND (:tipoId IS NULL OR tipoGastoId = :tipoId) AND syncStatus != :statusDeleted ORDER BY fecha DESC, id DESC"
    )
    suspend fun getByDateRangeAndTipo(
        desde: String,
        hasta: String,
        tipoId: Int?,
        statusDeleted: SyncStatus = SyncStatus.DELETED
    ): List<GastoEntity>

    // Nuevo: asignar cierre a gastos del usuario actual que no tengan cierre
    @Query(
        """
        UPDATE gastos
        SET cierreCajaId = :cierreId
        WHERE cierreCajaId IS NULL
          AND (syncStatus != :statusDeleted)
          AND usuarioId = :usuarioId
        """
    )
    suspend fun asignarCierreAGastosDeUsuario(usuarioId: Int, cierreId: Int, statusDeleted: SyncStatus = SyncStatus.DELETED): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GastoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<GastoEntity>)

    @Update
    suspend fun update(entity: GastoEntity)

    @Query("DELETE FROM gastos")
    suspend fun clearAll()

    @Query("SELECT * FROM gastos WHERE serverId = :serverId LIMIT 1")
    suspend fun findByServerId(serverId: Int): GastoEntity?

    @Transaction
    suspend fun upsertAll(items: List<GastoEntity>) {
        items.forEach { e ->
            val existente = e.serverId?.let { findByServerId(it) }
            val toInsert = if (existente != null) e.copy(id = existente.id) else e
            insert(toInsert)
        }
    }
}
