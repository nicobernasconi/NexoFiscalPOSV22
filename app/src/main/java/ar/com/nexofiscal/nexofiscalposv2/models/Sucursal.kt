// src/main/java/ar/com/nexofiscal/nexofiscalpos/models/Sucursal.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Sucursal {
    // --- Getters & Setters ---
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("empresa_id")
    var empresaId: Int = 0

    @SerializedName("nombre")
    var nombre: String? = null

    @SerializedName("direccion")
    var direccion: String? = null

    @SerializedName("telefono")
    var telefono: String? = null

    @SerializedName("email")
    var email: String? = null

    @SerializedName("contacto_nombre")
    var contactoNombre: String? = null

    @SerializedName("contacto_telefono")
    var contactoTelefono: String? = null

    @SerializedName("contacto_email")
    var contactoEmail: String? = null

    @SerializedName("referente_nombre")
    var referenteNombre: String? = null

    @SerializedName("referente_telefono")
    var referenteTelefono: String? = null

    @SerializedName("referente_email")
    var referenteEmail: String? = null

    override fun toString(): String {
        return "Sucursal{" +
                "nombre='" + nombre + '\'' +
                '}'
    }

    fun copy(id: Int = this.id, empresaId: Int = this.empresaId, nombre: String? = this.nombre, direccion: String? = this.direccion, telefono: String? = this.telefono, email: String? = this.email, contactoNombre: String? = this.contactoNombre, contactoTelefono: String? = this.contactoTelefono, contactoEmail: String? = this.contactoEmail, referenteNombre: String? = this.referenteNombre, referenteTelefono: String? = this.referenteTelefono, referenteEmail: String? = this.referenteEmail): Sucursal {
        val sucursal = Sucursal()
        sucursal.id = id
        sucursal.empresaId = empresaId
        sucursal.nombre = nombre
        sucursal.direccion = direccion
        sucursal.telefono = telefono
        sucursal.email = email
        sucursal.contactoNombre = contactoNombre
        sucursal.contactoTelefono = contactoTelefono
        sucursal.contactoEmail = contactoEmail
        sucursal.referenteNombre = referenteNombre
        sucursal.referenteTelefono = referenteTelefono
        sucursal.referenteEmail = referenteEmail
        return sucursal
    }
}
