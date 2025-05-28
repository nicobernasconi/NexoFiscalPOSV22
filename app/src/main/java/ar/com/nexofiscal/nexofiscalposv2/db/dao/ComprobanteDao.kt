// data/local/ComprobanteDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobanteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComprobanteDao {

        @Query("SELECT * FROM comprobantes")
        fun getAll(): Flow<List<ComprobanteEntity>>

        @Query("SELECT * FROM comprobantes WHERE id = :id")
        suspend fun getById(id: Int): ComprobanteEntity?

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(comprobante: ComprobanteEntity)

        @Update
        suspend fun update(comprobante: ComprobanteEntity)

        @Delete
        suspend fun delete(comprobante: ComprobanteEntity)

        @Query("DELETE FROM comprobantes")
        suspend fun clearAll()
    }