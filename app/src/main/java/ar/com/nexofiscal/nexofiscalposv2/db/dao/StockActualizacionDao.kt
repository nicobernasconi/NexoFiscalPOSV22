package ar.com.nexofiscal.nexofiscalposv2.db.dao

import androidx.room.*
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockActualizacionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface StockActualizacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stockActualizacion: StockActualizacionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stockActualizaciones: List<StockActualizacionEntity>)

    @Update
    suspend fun update(stockActualizacion: StockActualizacionEntity)

    @Delete
    suspend fun delete(stockActualizacion: StockActualizacionEntity)

    @Query("SELECT * FROM stock_actualizaciones WHERE id = :id")
    suspend fun getById(id: Int): StockActualizacionEntity?

    @Query("SELECT * FROM stock_actualizaciones WHERE productoId = :productoId AND sucursalId = :sucursalId")
    suspend fun getByProductoYSucursal(productoId: Int, sucursalId: Int): StockActualizacionEntity?

    @Query("SELECT * FROM stock_actualizaciones WHERE enviado = 0 ORDER BY fechaCreacion ASC")
    suspend fun getPendientesDeEnvio(): List<StockActualizacionEntity>

    @Query("SELECT * FROM stock_actualizaciones WHERE enviado = 1 ORDER BY fechaEnvio DESC")
    suspend fun getEnviados(): List<StockActualizacionEntity>

    @Query("SELECT * FROM stock_actualizaciones ORDER BY fechaCreacion DESC")
    fun getAllFlow(): Flow<List<StockActualizacionEntity>>

    @Query("SELECT * FROM stock_actualizaciones WHERE enviado = 0")
    fun getPendientesFlow(): Flow<List<StockActualizacionEntity>>

    @Query("UPDATE stock_actualizaciones SET enviado = 1, fechaEnvio = :fechaEnvio WHERE id = :id")
    suspend fun marcarComoEnviado(id: Int, fechaEnvio: Date = Date())

    @Query("UPDATE stock_actualizaciones SET intentos = intentos + 1, ultimoError = :error WHERE id = :id")
    suspend fun incrementarIntentos(id: Int, error: String?)

    @Query("DELETE FROM stock_actualizaciones WHERE enviado = 1 AND fechaEnvio < :fechaLimite")
    suspend fun limpiarEnviadosAntiguos(fechaLimite: Date)

    @Query("DELETE FROM stock_actualizaciones")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM stock_actualizaciones WHERE enviado = 0")
    fun contarPendientes(): Flow<Int>

    /**
     * Upsert: Inserta o actualiza una actualización de stock
     * Si ya existe para el mismo producto y sucursal, actualiza la cantidad
     */
    @Transaction
    suspend fun upsertActualizacion(productoId: Int, sucursalId: Int, cantidad: Double) {
        val existente = getByProductoYSucursal(productoId, sucursalId)
        if (existente != null) {
            update(existente.copy(
                cantidad = cantidad,
                fechaCreacion = Date(),
                enviado = false,
                fechaEnvio = null,
                intentos = 0,
                ultimoError = null
            ))
        } else {
            insert(StockActualizacionEntity(
                productoId = productoId,
                sucursalId = sucursalId,
                cantidad = cantidad
            ))
        }
    }
}
