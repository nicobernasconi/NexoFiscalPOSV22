// src/main/java/ar/com/nexofiscal/nexofiscalpos/models/Localidad.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Localidad {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("codigo_postal")
    var codigoPostal: String? = null

    @SerializedName("provincia")
    var provincia: Provincia? = null // Reutiliza tu modelo Provincia

    override fun toString(): String {
        return "Localidad{" +
                "nombre='" + nombre + '\'' +
                '}'
    }
}
