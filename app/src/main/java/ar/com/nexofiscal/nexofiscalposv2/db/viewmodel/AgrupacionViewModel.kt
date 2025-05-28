// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/AgrupacionViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.repository.AgrupacionRepository
import ar.com.nexofiscal.nexofiscalposv2.db.entity.AgrupacionEntity

class AgrupacionViewModel(context: Context) : ViewModel() {

    private val repo = AgrupacionRepository(AppDatabase.getInstance(context).agrupacionDao())

    val agrupaciones = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addOrUpdate(agrup: AgrupacionEntity) {
        viewModelScope.launch { repo.guardar(agrup) }
    }

    fun remove(agrup: AgrupacionEntity) {
        viewModelScope.launch { repo.eliminar(agrup) }
    }

    fun loadById(id: Int, callback: (AgrupacionEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
