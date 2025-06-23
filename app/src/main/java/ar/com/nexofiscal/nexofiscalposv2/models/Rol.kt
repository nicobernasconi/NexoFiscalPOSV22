package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Rol {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("descripcion")
    var descripcion: String? = null

    override fun toString(): String {
        return "Rol{" +
                "nombre='" + nombre + '\'' +
                '}'
    }

    fun copy(id: Int = this.id, nombre: String? = this.nombre, descripcion: String? = this.descripcion): Rol {
        val rol = Rol()
        rol.id = id
        rol.nombre = nombre
        rol.descripcion = descripcion
        return rol
    }
}
