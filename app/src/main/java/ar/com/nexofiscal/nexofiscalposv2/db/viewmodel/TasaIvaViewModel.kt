// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/TasaIvaViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.TasaIvaEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.TasaIvaRepository

class TasaIvaViewModel(context: Context) : ViewModel() {

    private val repo = TasaIvaRepository(
        AppDatabase.getInstance(context).tasaIvaDao()
    )

    /** Lista observable de tasas de IVA */
    val tasasIva = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza una tasa de IVA */
    fun save(item: TasaIvaEntity) {
        viewModelScope.launch { repo.guardar(item) }
    }

    /** Elimina una tasa de IVA */
    fun delete(item: TasaIvaEntity) {
        viewModelScope.launch { repo.eliminar(item) }
    }

    /** Carga una tasa de IVA por id */
    fun loadById(id: Int, callback: (TasaIvaEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
