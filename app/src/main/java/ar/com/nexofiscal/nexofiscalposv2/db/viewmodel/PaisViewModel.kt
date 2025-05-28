// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/PaisViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.PaisEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.PaisRepository

class PaisViewModel(context: Context) : ViewModel() {

    private val repo = PaisRepository(
        AppDatabase.getInstance(context).paisDao()
    )

    /** Lista observable de países */
    val paises = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza un país */
    fun save(p: PaisEntity) {
        viewModelScope.launch { repo.guardar(p) }
    }

    /** Elimina un país */
    fun delete(p: PaisEntity) {
        viewModelScope.launch { repo.eliminar(p) }
    }

    /** Carga un país por id */
    fun loadById(id: Int, callback: (PaisEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
