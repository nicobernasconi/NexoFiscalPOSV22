// 2. DAO
// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/TipoDocumentoDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoDocumentoEntity

@Dao
interface TipoDocumentoDao {

    @Query("SELECT * FROM tipos_documento")
    fun getAll(): Flow<List<TipoDocumentoEntity>>

    @Query("SELECT * FROM tipos_documento WHERE id = :id")
    suspend fun getById(id: Int): TipoDocumentoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TipoDocumentoEntity)

    @Update
    suspend fun update(item: TipoDocumentoEntity)

    @Delete
    suspend fun delete(item: TipoDocumentoEntity)

    @Query("DELETE FROM tipos_documento")
    suspend fun clearAll()
}
