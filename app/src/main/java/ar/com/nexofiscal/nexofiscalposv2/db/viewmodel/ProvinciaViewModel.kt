// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/ProvinciaViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.ProvinciaEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.ProvinciaRepository

class ProvinciaViewModel(context: Context) : ViewModel() {

    private val repo = ProvinciaRepository(
        AppDatabase.getInstance(context).provinciaDao()
    )

    /** Lista observable de provincias */
    val provincias = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza una provincia */
    fun save(p: ProvinciaEntity) {
        viewModelScope.launch { repo.guardar(p) }
    }

    /** Elimina una provincia */
    fun delete(p: ProvinciaEntity) {
        viewModelScope.launch { repo.eliminar(p) }
    }

    /** Carga una provincia por id */
    fun loadById(id: Int, callback: (ProvinciaEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
