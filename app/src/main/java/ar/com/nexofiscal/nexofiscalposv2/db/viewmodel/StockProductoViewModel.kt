// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/StockProductoViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.StockProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.StockProductoRepository

class StockProductoViewModel(context: Context) : ViewModel() {

    private val repo = StockProductoRepository(
        AppDatabase.getInstance(context).stockProductoDao()
    )

    /** Lista observable de stock de productos */
    val stockProductos = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza un registro */
    fun save(item: StockProductoEntity) {
        viewModelScope.launch { repo.guardar(item) }
    }

    /** Elimina un registro */
    fun delete(item: StockProductoEntity) {
        viewModelScope.launch { repo.eliminar(item) }
    }

    /** Carga un registro por id */
    fun loadById(id: Int, callback: (StockProductoEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
