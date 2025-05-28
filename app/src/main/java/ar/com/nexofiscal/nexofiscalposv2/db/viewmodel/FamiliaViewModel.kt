// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/FamiliaViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.FamiliaEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.FamiliaRepository

class FamiliaViewModel(context: Context) : ViewModel() {

    private val repo = FamiliaRepository(
        AppDatabase.getInstance(context).familiaDao()
    )

    /** Lista observable de familias */
    val familias = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza una familia */
    fun save(f: FamiliaEntity) {
        viewModelScope.launch { repo.guardar(f) }
    }

    /** Elimina una familia */
    fun delete(f: FamiliaEntity) {
        viewModelScope.launch { repo.eliminar(f) }
    }

    /** Carga una familia por id */
    fun loadById(id: Int, callback: (FamiliaEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
