package ar.com.nexofiscal.nexofiscalposv2.db.entity

enum class SyncStatus {
    SYNCED,  // Sincronizado
    CREATED, // Creado localmente, pendiente de POST
    UPDATED, // Modificado localmente, pendiente de PUT
    DELETED  // Borrado localmente, pendiente de DELETE
}