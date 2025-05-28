// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/UnidadViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UnidadEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.UnidadRepository

class UnidadViewModel(context: Context) : ViewModel() {

    private val repo = UnidadRepository(
        AppDatabase.getInstance(context).unidadDao()
    )

    /** Lista observable de unidades */
    val unidades = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o reemplaza una unidad */
    fun save(item: UnidadEntity) {
        viewModelScope.launch { repo.guardar(item) }
    }

    /** Elimina una unidad */
    fun delete(item: UnidadEntity) {
        viewModelScope.launch { repo.eliminar(item) }
    }

    /** Carga una unidad por id */
    fun loadById(id: Int, callback: (UnidadEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
