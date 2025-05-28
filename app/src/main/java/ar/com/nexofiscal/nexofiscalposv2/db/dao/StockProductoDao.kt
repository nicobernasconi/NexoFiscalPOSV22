// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/StockProductoDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockProductoEntity

@Dao
interface StockProductoDao {

    @Query("SELECT * FROM stock_productos")
    fun getAll(): Flow<List<StockProductoEntity>>

    @Query("SELECT * FROM stock_productos WHERE id = :id")
    suspend fun getById(id: Int): StockProductoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StockProductoEntity)

    @Update
    suspend fun update(item: StockProductoEntity)

    @Delete
    suspend fun delete(item: StockProductoEntity)

    @Query("DELETE FROM stock_productos")
    suspend fun clearAll()
}
