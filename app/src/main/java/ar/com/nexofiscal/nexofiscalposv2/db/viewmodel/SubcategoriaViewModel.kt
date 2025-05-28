// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/SubcategoriaViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SubcategoriaEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.SubcategoriaRepository

class SubcategoriaViewModel(context: Context) : ViewModel() {

    private val repo = SubcategoriaRepository(
        AppDatabase.getInstance(context).subcategoriaDao()
    )

    /** Lista observable de subcategorías */
    val subcategorias = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza una subcategoría */
    fun save(item: SubcategoriaEntity) {
        viewModelScope.launch { repo.guardar(item) }
    }

    /** Elimina una subcategoría */
    fun delete(item: SubcategoriaEntity) {
        viewModelScope.launch { repo.eliminar(item) }
    }

    /** Carga una subcategoría por id */
    fun loadById(id: Int, callback: (SubcategoriaEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
