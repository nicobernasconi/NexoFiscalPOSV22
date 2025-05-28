// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/ProveedorViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProveedorEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.ProveedorRepository

class ProveedorViewModel(context: Context) : ViewModel() {

    private val repo = ProveedorRepository(
        AppDatabase.getInstance(context).proveedorDao()
    )

    /** Lista observable de proveedores */
    val proveedores = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza un proveedor */
    fun save(p: ProveedorEntity) {
        viewModelScope.launch { repo.guardar(p) }
    }

    /** Elimina un proveedor */
    fun delete(p: ProveedorEntity) {
        viewModelScope.launch { repo.eliminar(p) }
    }

    /** Carga un proveedor por id */
    fun loadById(id: Int, callback: (ProveedorEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
