// src/main/java/ar/com/nexofiscal/nexofiscalposv2/db/dao/ClienteDao.kt
package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ClienteEntity

@Dao
interface ClienteDao {

    @Query("SELECT * FROM clientes")
    fun getAll(): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun getById(id: Int): ClienteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cliente: ClienteEntity)

    @Update
    suspend fun update(cliente: ClienteEntity)

    @Delete
    suspend fun delete(cliente: ClienteEntity)

    @Query("DELETE FROM clientes")
    suspend fun clearAll()
}
