// src/main/java/ar/com/nexofiscal/nexofiscalpos/models/Categoria.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Categoria {
    // Getters & Setters
    @SerializedName("id")
    var id: Int? = null

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("se_imprime")
    var seImprime: Int? = null


    override fun toString(): String {
        return "Categoria{" +
                "nombre='" + nombre + '\'' +
                '}'
    }

    fun copy(id: Int? = this.id, nombre: String? = this.nombre, seImprime: Int? = this.seImprime): Categoria {
        val categoria = Categoria()
        categoria.id = id
        categoria.nombre = nombre
        categoria.seImprime = seImprime
        return categoria
    }
}
