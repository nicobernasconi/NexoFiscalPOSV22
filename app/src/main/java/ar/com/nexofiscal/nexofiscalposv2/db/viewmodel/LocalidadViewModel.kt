// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/LocalidadViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.LocalidadEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.LocalidadRepository

class LocalidadViewModel(context: Context) : ViewModel() {

    private val repo = LocalidadRepository(
        AppDatabase.getInstance(context).localidadDao()
    )

    /** Lista observable de localidades */
    val localidades = repo.todas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o actualiza una localidad */
    fun save(l: LocalidadEntity) {
        viewModelScope.launch { repo.guardar(l) }
    }

    /** Elimina una localidad */
    fun delete(l: LocalidadEntity) {
        viewModelScope.launch { repo.eliminar(l) }
    }

    /** Carga una localidad por id */
    fun loadById(id: Int, callback: (LocalidadEntity?)->Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
