// src/main/java/ar/com/nexofiscal/nexofiscalposv2/ui/viewmodel/UsuarioViewModel.kt
package ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import ar.com.nexofiscal.nexofiscalposv2.db.entity.UsuarioEntity
import ar.com.nexofiscal.nexofiscalposv2.repository.UsuarioRepository

class UsuarioViewModel(context: Context) : ViewModel() {

    private val repo = UsuarioRepository(
        AppDatabase.getInstance(context).usuarioDao()
    )

    /** Lista observable de usuarios */
    val usuarios = repo.todos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Guarda o reemplaza un usuario */
    fun save(usuario: UsuarioEntity) {
        viewModelScope.launch { repo.guardar(usuario) }
    }

    /** Elimina un usuario */
    fun delete(usuario: UsuarioEntity) {
        viewModelScope.launch { repo.eliminar(usuario) }
    }

    /** Carga un usuario por id */
    fun loadById(id: Int, callback: (UsuarioEntity?) -> Unit) {
        viewModelScope.launch {
            callback(repo.porId(id))
        }
    }
}
