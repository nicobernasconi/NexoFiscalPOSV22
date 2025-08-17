package ar.com.nexofiscal.nexofiscalposv2.managers

/**
 * Servicio centralizado para manejo de permisos de usuario.
 *
 * Fuente de verdad: SessionManager (prefs -> JSON de permisos por entidad/acción).
 * Este servicio provee una API simple y reutilizable para consultar permisos
 * y actualizar el set actual (por ejemplo, luego del login o cambio de rol).
 */
object PermissionService {

    /** Devuelve true si el usuario actual puede ejecutar [action] sobre [entity]. */
    fun can(entity: String, action: String): Boolean = SessionManager.hasPermission(entity, action)

    /** Devuelve true si el usuario posee AL MENOS uno de los pares (entity, action) provistos. */
    fun anyOf(vararg requirements: Pair<String, String>): Boolean = requirements.any { (e, a) -> can(e, a) }

    /** Devuelve true si el usuario posee TODOS los pares (entity, action) provistos. */
    fun allOf(vararg requirements: Pair<String, String>): Boolean = requirements.all { (e, a) -> can(e, a) }

    /** Limpia todos los permisos en memoria y persistencia. */
    fun clear() = SessionManager.clearPermissions()

    /** Carga/actualiza el JSON de permisos (formato esperado: objeto JSON plano con arrays de acciones por entidad). */
    fun loadFromJson(permissionsJson: String?) = SessionManager.setPermissionsJson(permissionsJson)

    /** Verificación simple por rol (por id). Útil cuando ciertas pantallas se protegen por rol completo. */
    fun hasRoleId(roleId: Int): Boolean = SessionManager.rolId == roleId
}

