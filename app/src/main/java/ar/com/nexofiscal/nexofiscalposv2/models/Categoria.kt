package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Categoria {
    var localId: Int = 0 // <-- AÑADIDO
    @SerializedName("id")
    var id: Int? = null
    @SerializedName("nombre")
    var nombre: String? = null
    @SerializedName("se_imprime")
    var seImprime: Int? = null

    override fun toString(): String {
        return "Categoria(nombre='$nombre')"
    }

    fun copy(
        localId: Int = this.localId, // <-- AÑADIDO
        id: Int? = this.id,
        nombre: String? = this.nombre,
        seImprime: Int? = this.seImprime
    ): Categoria {
        val categoria = Categoria()
        categoria.localId = localId // <-- AÑADIDO
        categoria.id = id
        categoria.nombre = nombre
        categoria.seImprime = seImprime
        return categoria
    }
}