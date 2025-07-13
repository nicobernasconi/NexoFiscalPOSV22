// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/edit/EntityEditModels.kt
package ar.com.nexofiscal.nexofiscalposv2.screens.edit

import androidx.compose.runtime.Composable

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()
}

/**
 * Describe un campo del formulario.
 * @param onEntityUpdated Una función que recibe OTRA FUNCIÓN que describe el cambio.
 * Esto asegura que la actualización se haga sobre el estado más reciente.
 */
data class FieldDescriptor<T>(
    val id: String,
    val label: String,
    val editorContent: @Composable (
        currentEntity: T,
        onEntityUpdated: (updateAction: (T) -> T) -> Unit,
        isReadOnly: Boolean,
        error: String?
    ) -> Unit,
    val validator: (entity: T) -> ValidationResult = { ValidationResult.Valid },
    val isReadOnly: (entity: T) -> Boolean = { false }
)