// src/main/java/ar/com/nexofiscal/nexofiscalpos/models/Subcategoria.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Subcategoria {
    // Getters & Setters
    @SerializedName("id")
    var id: Int? = null

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("se_imprime")
    var seImprime: Boolean? = null

    override fun toString(): String {
        return "Subcategoria{" +
                "nombre='" + nombre + '\'' +
                '}'
    }
}
