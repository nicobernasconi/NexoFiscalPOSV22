// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/TipoIvaViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoIvaEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.TipoIvaRepository

class TipoIvaViewModel(context: Context) : ViewModel() {

    private val repo = TipoIvaRepository(
        AppDatabase.getInstance(context).tipoIvaDao()
    )

    /** Lista observable de tipos de IVA */
    val tiposIva = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o reemplaza un tipo de IVA */
    fun save(item: TipoIvaEntity) {
        viewModelScope.launch { repo.guardar(item) }
    }

    /** Elimina un tipo de IVA */
    fun delete(item: TipoIvaEntity) {
        viewModelScope.launch { repo.eliminar(item) }
    }

    /** Carga un tipo de IVA por id */
    fun loadById(id: Int, callback: (TipoIvaEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
