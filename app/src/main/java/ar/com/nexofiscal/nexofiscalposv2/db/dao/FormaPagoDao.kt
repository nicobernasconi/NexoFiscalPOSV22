// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/FormaPagoDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FormaPagoEntity

@Dao
interface FormaPagoDao {

    @Query("SELECT * FROM formas_pago")
    fun getAll(): Flow<List<FormaPagoEntity>>

    @Query("SELECT * FROM formas_pago WHERE id = :id")
    suspend fun getById(id: Int): FormaPagoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(forma: FormaPagoEntity)

    @Update
    suspend fun update(forma: FormaPagoEntity)

    @Delete
    suspend fun delete(forma: FormaPagoEntity)

    @Query("DELETE FROM formas_pago")
    suspend fun clearAll()
}
