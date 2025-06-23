// src/main/java/ar/com/nexofiscal/nexofiscalpos/models/Pais.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Pais {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    override fun toString(): String {
        return "Pais{" +
                "nombre='" + nombre + '\'' +
                '}'
    }

    fun copy(id: Int = this.id, nombre: String? = this.nombre): Pais {
        val pais = Pais()
        pais.id = id
        pais.nombre = nombre
        return pais
    }
}
