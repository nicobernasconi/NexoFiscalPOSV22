// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/VendedorViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.VendedorEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.VendedorRepository

class VendedorViewModel(context: Context) : ViewModel() {

    private val repo = VendedorRepository(
        AppDatabase.getInstance(context).vendedorDao()
    )

    /** Lista observable de vendedores */
    val vendedores = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o reemplaza un vendedor */
    fun save(item: VendedorEntity) {
        viewModelScope.launch { repo.guardar(item) }
    }

    /** Elimina un vendedor */
    fun delete(item: VendedorEntity) {
        viewModelScope.launch { repo.eliminar(item) }
    }

    /** Carga un vendedor por id */
    fun loadById(id: Int, callback: (VendedorEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
