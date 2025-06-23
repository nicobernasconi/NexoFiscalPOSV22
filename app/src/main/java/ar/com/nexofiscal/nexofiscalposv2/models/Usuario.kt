// Usuario.java
package ar.com.nexofiscal.nexofiscalposv2.models

import com.google.gson.annotations.SerializedName

class Usuario {
    // Getters & Setters
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("nombre_usuario")
    var nombreUsuario: String? = null

    @SerializedName("password")
    var password: String? = null

    @SerializedName("nombre_completo")
    var nombreCompleto: String? = null

    @SerializedName("activo")
    var activo: Int? = null // puede ser null

    @SerializedName("empresa_id")
    var empresaId: Int = 0

    @SerializedName("rol")
    var rol: Rol? = null

    @SerializedName("sucursal")
    var sucursal: Sucursal? = null

    @SerializedName("vendedor")
    var vendedor: Vendedor? = null

    override fun toString(): String {
        return "Usuario{" +
                "nombreUsuario='" + nombreUsuario + '\'' +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                '}'
    }

    fun copy(id: Int = this.id,
             nombreUsuario: String? = this.nombreUsuario,
             password: String? = this.password,
             nombreCompleto: String? = this.nombreCompleto,
             activo: Int? = this.activo,
             empresaId: Int = this.empresaId,
             rol: Rol? = this.rol,
             sucursal: Sucursal? = this.sucursal,
             vendedor: Vendedor? = this.vendedor): Usuario {
        val usuario = Usuario()
        usuario.id = id
        usuario.nombreUsuario = nombreUsuario
        usuario.password = password
        usuario.nombreCompleto = nombreCompleto
        usuario.activo = activo
        usuario.empresaId = empresaId
        usuario.rol = rol
        usuario.sucursal = sucursal
        usuario.vendedor = vendedor
        return usuario
    }

}

