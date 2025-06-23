package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoConDetalles
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    // --- CAMBIO: Las consultas ahora excluyen los registros marcados para borrar ---
    @Transaction
    @Query("SELECT * FROM productos WHERE syncStatus != :statusDeleted ORDER BY descripcion ASC")
    fun getPagingSourceWithDetails(statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ProductoConDetalles>

    @Transaction
    @Query("SELECT * FROM productos WHERE (descripcion LIKE :query OR codigo LIKE :query OR codigoBarra LIKE :query) AND syncStatus != :statusDeleted ORDER BY descripcion ASC")
    fun searchPagingSourceWithDetails(query: String, statusDeleted: SyncStatus = SyncStatus.DELETED): PagingSource<Int, ProductoConDetalles>

    @Transaction
    @Query("SELECT * FROM productos WHERE favorito = 1 AND syncStatus != :statusDeleted ORDER BY descripcion ASC")
    fun getFavoritosWithDetails(statusDeleted: SyncStatus = SyncStatus.DELETED): Flow<List<ProductoConDetalles>>

    @Query("SELECT * FROM productos WHERE (codigoBarra = :barcode OR codigoBarra2 = :barcode) AND syncStatus != :statusDeleted LIMIT 1")
    suspend fun findByBarcode(barcode: String, statusDeleted: SyncStatus = SyncStatus.DELETED): ProductoEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM productos WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<ProductoEntity>

    @Query("UPDATE productos SET serverId = :serverId, syncStatus = :statusSynced WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: Int, serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("UPDATE productos SET syncStatus = :statusSynced WHERE serverId = :serverId")
    suspend fun updateStatusToSyncedByServerId(serverId: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM productos WHERE id = :localId")
    suspend fun deleteByLocalId(localId: Int)

    // --- MÉTODOS EXISTENTES ---

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun getById(id: Int): ProductoEntity?

    @Query("SELECT * FROM productos WHERE favorito = 1 ORDER BY descripcion ASC")
    fun getFavoritos(): Flow<List<ProductoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(producto: ProductoEntity)

    @Transaction
    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun getConDetallesById(id: Int): ProductoConDetalles?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ProductoEntity>)

    @Update
    suspend fun update(producto: ProductoEntity)

    @Query("DELETE FROM productos")
    suspend fun clearAll()
}