package ar.com.nexofiscal.nexofiscalposv2.screens.edit

import androidx.compose.runtime.Composable

/**
 * Representa el resultado de una validación de campo.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()
}

/**
 * Describe un campo individual y cómo editarlo en la pantalla de edición genérica.
 * @param T El tipo de la entidad que se está editando.
 */
data class FieldDescriptor<T>(
    val id: String, // Identificador único para el campo (usado como key y para errores)
    val label: String, // Etiqueta que se muestra para el campo
    // El Composable que renderiza el control de edición para este campo:
    val editorContent: @Composable (
        currentEntity: T,
        onEntityUpdated: (updatedEntity: T) -> Unit, // Callback para notificar un cambio en la entidad
        isReadOnly: Boolean,
        error: String? // Mensaje de error actual para este campo, si existe
    ) -> Unit,
    val validator: (entity: T) -> ValidationResult = { ValidationResult.Valid },
    val isReadOnly: (entity: T) -> Boolean = { false }
)