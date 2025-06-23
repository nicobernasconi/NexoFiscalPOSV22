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
    fun copy(id: Int = this.id, nombre: String? = this.nombre, codigoPostal: String? = this.codigoPostal, provincia: Provincia? = this.provincia): Localidad {
        val localidad = Localidad()
        localidad.id = id
        localidad.nombre = nombre
        localidad.codigoPostal = codigoPostal
        localidad.provincia = provincia?.copy() // Asumiendo que Provincia tiene un método copy()
        return localidad
    }
}
