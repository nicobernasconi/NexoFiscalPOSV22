// 5. ViewModel
// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/TipoDocumentoViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoDocumentoEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.TipoDocumentoRepository

class TipoDocumentoViewModel(context: Context) : ViewModel() {

    private val repo = TipoDocumentoRepository(
        AppDatabase.getInstance(context).tipoDocumentoDao()
    )

    /** Lista observable de tipos de documento */
    val tiposDocumento = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza un tipo de documento */
    fun save(item: TipoDocumentoEntity) {
        viewModelScope.launch { repo.guardar(item) }
    }

    /** Elimina un tipo de documento */
    fun delete(item: TipoDocumentoEntity) {
        viewModelScope.launch { repo.eliminar(item) }
    }

    /** Carga un tipo de documento por id */
    fun loadById(id: Int, callback: (TipoDocumentoEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
