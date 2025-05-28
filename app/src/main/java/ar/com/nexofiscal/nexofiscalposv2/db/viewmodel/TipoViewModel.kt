// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/TipoViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.TipoRepository

class TipoViewModel(context: Context) : ViewModel() {

    private val repo = TipoRepository(
        AppDatabase.getInstance(context).tipoDao()
    )

    /** Lista observable de tipos */
    val tipos = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza un tipo */
    fun save(item: TipoEntity) {
        viewModelScope.launch { repo.guardar(item) }
    }

    /** Elimina un tipo */
    fun delete(item: TipoEntity) {
        viewModelScope.launch { repo.eliminar(item) }
    }

    /** Carga un tipo por id */
    fun loadById(id: Int, callback: (TipoEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
