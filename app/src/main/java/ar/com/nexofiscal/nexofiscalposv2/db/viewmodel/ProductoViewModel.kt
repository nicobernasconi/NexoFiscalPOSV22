// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/ProductoViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProductoEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.ProductoRepository

class ProductoViewModel(context: Context) : ViewModel() {

    private val repo = ProductoRepository(
        AppDatabase.getInstance(context).productoDao()
    )

    /** Lista observable de productos */
    val productos = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza un producto */
    fun save(p: ProductoEntity) {
        viewModelScope.launch { repo.guardar(p) }
    }

    /** Elimina un producto */
    fun delete(p: ProductoEntity) {
        viewModelScope.launch { repo.eliminar(p) }
    }

    /** Carga un producto por id */
    fun loadById(id: Int, callback: (ProductoEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
