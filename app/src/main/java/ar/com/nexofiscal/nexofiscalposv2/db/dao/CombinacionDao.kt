package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CombinacionEntity
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CombinacionDao {

    // --- CAMBIO: La consulta ahora excluye los registros marcados para borrar ---
    @Query("SELECT * FROM combinaciones WHERE syncStatus != :statusDeleted")
    fun getAll(statusDeleted: SyncStatus = SyncStatus.DELETED): Flow<List<CombinacionEntity>>

    @Query("SELECT * FROM combinaciones WHERE uid = :uid")
    suspend fun getByUid(uid: Int): CombinacionEntity?

    // --- FUNCIONES NUEVAS PARA SINCRONIZACIÓN ---

    @Query("SELECT * FROM combinaciones WHERE syncStatus != :statusSynced")
    suspend fun getUnsynced(statusSynced: SyncStatus = SyncStatus.SYNCED): List<CombinacionEntity>

    @Query("UPDATE combinaciones SET syncStatus = :statusSynced WHERE uid = :localUid")
    suspend fun updateStatusToSynced(localUid: Int, statusSynced: SyncStatus = SyncStatus.SYNCED)

    @Query("DELETE FROM combinaciones WHERE uid = :localUid")
    suspend fun deleteByLocalUid(localUid: Int)

    // --- MÉTODOS EXISTENTES (con pequeños ajustes) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comb: CombinacionEntity)

    @Update
    suspend fun update(comb: CombinacionEntity)

    @Delete
    suspend fun delete(comb: CombinacionEntity)

    @Query("DELETE FROM combinaciones")
    suspend fun clearAll()
}