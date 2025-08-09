package ar.com.nexofiscal.nexofiscalposv2.db.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoConStockCompleto
import ar.com.nexofiscal.nexofiscalposv2.db.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductoRepository

    init {
        val database = AppDatabase.getInstance(application) // Usar getInstance en lugar de getDatabase
        repository = ProductoRepository(database.productoDao())

        // Diagnóstico: verificar datos en las tablas
        viewModelScope.launch {
            try {
                val productosCount = repository.getProductosCount()
                val stockCount = repository.getStockProductosCount()
                Log.d("StockViewModel", "Productos en DB: $productosCount")
                Log.d("StockViewModel", "Registros de stock en DB: $stockCount")
            } catch (e: Exception) {
                Log.e("StockViewModel", "Error al obtener counts: ${e.message}")
            }
        }
    }

    // Estado para la lista de productos con stock
    val productosConStock: StateFlow<List<ProductoConStockCompleto>> = repository
        .getProductosConStockCompleto()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Estado para filtros de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Estado para productos filtrados usando combine
    val filteredProducts: StateFlow<List<ProductoConStockCompleto>> = combine(
        productosConStock,
        searchQuery
    ) { productos, query ->
        Log.d("StockViewModel", "Productos obtenidos: ${productos.size}")
        if (productos.isNotEmpty()) {
            Log.d("StockViewModel", "Primer producto: ${productos.first().descripcion}, Stock: ${productos.first().stockActual}")
        }

        if (query.isBlank()) {
            productos
        } else {
            productos.filter { producto ->
                producto.descripcion?.contains(query, ignoreCase = true) == true ||
                producto.codigo?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
