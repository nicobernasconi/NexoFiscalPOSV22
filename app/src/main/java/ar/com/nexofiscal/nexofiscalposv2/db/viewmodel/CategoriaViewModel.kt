// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/CategoriaViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CategoriaEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.CategoriaRepository

class CategoriaViewModel(context: Context) : ViewModel() {

    private val repo = CategoriaRepository(
        AppDatabase.getInstance(context).categoriaDao()
    )

    val categorias = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addOrUpdate(c: CategoriaEntity) {
        viewModelScope.launch { repo.guardar(c) }
    }

    fun remove(c: CategoriaEntity) {
        viewModelScope.launch { repo.eliminar(c) }
    }

    fun loadById(id: Int, callback: (CategoriaEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
