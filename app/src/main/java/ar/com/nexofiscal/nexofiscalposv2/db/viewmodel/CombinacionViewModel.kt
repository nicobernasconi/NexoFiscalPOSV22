// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/CombinacionViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.CombinacionEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.CombinacionRepository

class CombinacionViewModel(context: Context) : ViewModel() {

    private val repo = CombinacionRepository(
        AppDatabase.getInstance(context).combinacionDao()
    )

    /** Observa la lista de combinaciones */
    val combinaciones = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza */
    fun save(c: CombinacionEntity) {
        viewModelScope.launch { repo.guardar(c) }
    }

    /** Elimina */
    fun delete(c: CombinacionEntity) {
        viewModelScope.launch { repo.eliminar(c) }
    }

    /** Busca por UID y devuelve en callback */
    fun loadByUid(uid: Int, callback: (CombinacionEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porUid(uid))
        }
    }
}
