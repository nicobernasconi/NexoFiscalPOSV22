// src/main/java/ar/com/nexofiscal/nexofiscalpos/models/Agrupacion.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Agrupacion {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("numero")
    var numero: Int? = null // puede ser null

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("color")
    var color: String? = null

    @SerializedName("icono")
    var icono: String? = null

    constructor()

    constructor(id: Int, numero: Int?, nombre: String?, color: String?, icono: String?) {
        this.id = id
        this.numero = numero
        this.nombre = nombre
        this.color = color
        this.icono = icono
    }

    override fun toString(): String {
        return "Agrupacion{" +
                "id=" + id +
                ", numero=" + numero +
                ", nombre='" + nombre + '\'' +
                ", color='" + color + '\'' +
                ", icono='" + icono + '\'' +
                '}'
    }
}
