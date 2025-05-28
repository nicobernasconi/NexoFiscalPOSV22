// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/ComprobanteViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ComprobanteEntity
import ar.com.nexofiscal.nexofiscalposv2.db.repository.ComprobanteRepository

class ComprobanteViewModel(context: Context) : ViewModel() {

    private val repo = ComprobanteRepository(
        AppDatabase.getInstance(context).comprobanteDao()
    )

    /** Lista observable de comprobantes */
    val comprobantes = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza un comprobante */
    fun save(c: ComprobanteEntity) {
        viewModelScope.launch { repo.guardar(c) }
    }

    /** Elimina un comprobante */
    fun delete(c: ComprobanteEntity) {
        viewModelScope.launch { repo.eliminar(c) }
    }

    /** Carga un comprobante por id */
    fun loadById(id: Int, callback: (ComprobanteEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}