package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockActualizacionEntity
import ar.com.nexofiscal.nexofiscalposv2.screens.EstadisticasStock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockMovimientosViewModel(application: Application) : AndroidViewModel(application) {

    private val stockActualizacionDao = AppDatabase.getInstance(application).stockActualizacionDao()

    private val _movimientos = MutableStateFlow<List<StockActualizacionEntity>>(emptyList())
    val movimientos: StateFlow<List<StockActualizacionEntity>> = _movimientos.asStateFlow()

    private val _estadisticas = MutableStateFlow(EstadisticasStock())
    val estadisticas: StateFlow<EstadisticasStock> = _estadisticas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    companion object {
        private const val TAG = "StockMovimientosViewModel"
    }

    fun cargarMovimientos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Cargando movimientos de stock...")

                // Obtener todos los movimientos ordenados por fecha
                val todosMovimientos = stockActualizacionDao.getAllFlow()

                todosMovimientos.collect { lista ->
                    _movimientos.value = lista
                    Log.d(TAG, "Movimientos cargados: ${lista.size}")

                    // Calcular estadísticas
                    calcularEstadisticas(lista)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar movimientos: ${e.message}", e)
                _movimientos.value = emptyList()
                _estadisticas.value = EstadisticasStock()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun calcularEstadisticas(movimientos: List<StockActualizacionEntity>) {
        try {
            val total = movimientos.size
            val pendientes = movimientos.count { !it.enviado }
            val enviados = movimientos.count { it.enviado }

            _estadisticas.value = EstadisticasStock(
                totalMovimientos = total,
                pendientesEnvio = pendientes,
                enviados = enviados
            )

            Log.d(TAG, "Estadísticas: Total=$total, Pendientes=$pendientes, Enviados=$enviados")

        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular estadísticas: ${e.message}", e)
        }
    }

    /**
     * Función para crear movimientos de prueba (solo para testing)
     */
    fun crearMovimientosPrueba() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creando movimientos de prueba...")

                val movimientosPrueba = listOf(
                    StockActualizacionEntity(
                        productoId = 101,
                        sucursalId = 1,
                        cantidad = -5.0, // Venta
                        enviado = false
                    ),
                    StockActualizacionEntity(
                        productoId = 102,
                        sucursalId = 1,
                        cantidad = -3.0, // Venta
                        enviado = true
                    ),
                    StockActualizacionEntity(
                        productoId = 103,
                        sucursalId = 1,
                        cantidad = 10.0, // Reposición
                        enviado = false
                    )
                )

                stockActualizacionDao.insertAll(movimientosPrueba)
                Log.d(TAG, "Movimientos de prueba creados")

                // Recargar movimientos
                cargarMovimientos()

            } catch (e: Exception) {
                Log.e(TAG, "Error al crear movimientos de prueba: ${e.message}", e)
            }
        }
    }

    /**
     * Función para limpiar todos los movimientos (solo para testing)
     */
    fun limpiarMovimientos() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Limpiando todos los movimientos...")
                stockActualizacionDao.clearAll()
                cargarMovimientos()
            } catch (e: Exception) {
                Log.e(TAG, "Error al limpiar movimientos: ${e.message}", e)
            }
        }
    }
}
