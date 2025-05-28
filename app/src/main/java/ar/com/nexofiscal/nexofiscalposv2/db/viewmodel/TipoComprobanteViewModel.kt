// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/TipoComprobanteViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TipoComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.TipoComprobanteRepository

class TipoComprobanteViewModel(context: Context) : ViewModel() {

    private val repo = TipoComprobanteRepository(
        AppDatabase.getInstance(context).tipoComprobanteDao()
    )

    /** Lista observable de tipos de comprobante */
    val tiposComprobante = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza un tipo de comprobante */
    fun save(item: TipoComprobanteEntity) {
        viewModelScope.launch { repo.guardar(item) }
    }

    /** Elimina un tipo de comprobante */
    fun delete(item: TipoComprobanteEntity) {
        viewModelScope.launch { repo.eliminar(item) }
    }

    /** Carga un tipo por id */
    fun loadById(id: Int, callback: (TipoComprobanteEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
