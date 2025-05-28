// src/main/java/ar/com/nexofiscal/nexofiscalpos/models/Provincia.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Provincia {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("pais")
    var pais: Pais? = null // Reutiliza el modelo Pais

    override fun toString(): String {
        return "Provincia{" +
                "nombre='" + nombre + '\'' +
                '}'
    }
}

