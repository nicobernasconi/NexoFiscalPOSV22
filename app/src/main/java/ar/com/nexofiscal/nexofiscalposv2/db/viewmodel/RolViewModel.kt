// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/RolViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.RolEntity
import ar.com.nexofiscal.nexofiscalposv2.db.repository.RolRepository


class RolViewModel(context: Context) : ViewModel() {

    private val repo = RolRepository(
        AppDatabase.getInstance(context).rolDao()
    )

    /** Lista observable de roles */
    val roles = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza un rol */
    fun save(r: RolEntity) {
        viewModelScope.launch { repo.guardar(r) }
    }

    /** Elimina un rol */
    fun delete(r: RolEntity) {
        viewModelScope.launch { repo.eliminar(r) }
    }

    /** Carga un rol por id */
    fun loadById(id: Int, callback: (RolEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
