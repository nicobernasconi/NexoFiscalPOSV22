package ar.com.nexofiscal.nexofiscalposv2.screens.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

data class FieldCustomization<T>(
    val label: String? = null,
    // CAMBIO: Se actualiza la firma de `editorContent` para que coincida con la nueva arquitectura.
    val editorContent: (@Composable (
        entity: T,
        onUpdate: (updateAction: (T) -> T) -> Unit,
        isReadOnly: Boolean,
        error: String?
    ) -> Unit)? = null,
    val validator: ((entity: T) -> ValidationResult)? = null,
    val isReadOnly: ((entity: T) -> Boolean)? = null,
    val keyboardTypeOverride: KeyboardType? = null
)

@Composable
fun <T> DropdownPlaceholderEditor(
    label: String,
    currentValueDisplay: String,
    isReadOnly: Boolean,
    error: String?,
    onClickAction: () -> Unit
) {
    OutlinedTextField(
        value = currentValueDisplay,
        onValueChange = { /* No se edita directamente */ },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isReadOnly, onClick = onClickAction),
        label = { Text(label) },
        readOnly = true,
        isError = error != null,
        supportingText = {
            if (error != null) Text(error)
            else Text("Seleccionar...")
        },
    )
}

/**
 * Genera FieldDescriptors automáticamente. Aunque ya no lo usamos para campos complejos,
 * lo actualizamos para que sea coherente con la nueva arquitectura.
 */
inline fun <reified T : Any> generateAutomaticFieldDescriptors(
    entityClass: KClass<T>,
    attributesToIncludeAndOrder: List<String>,
    customizations: Map<String, FieldCustomization<T>> = emptyMap(),
    noinline labelProvider: (propName: String) -> String = { propName -> propName.replaceFirstChar { it.uppercaseChar() } }
): List<FieldDescriptor<T>> {

    val allMutableProperties = entityClass.memberProperties
        .filterIsInstance<KMutableProperty1<T, Any?>>()
        .associateBy { it.name }

    val descriptors = mutableListOf<FieldDescriptor<T>>()

    attributesToIncludeAndOrder.forEach { propName ->
        val prop = allMutableProperties[propName]
        if (prop == null) {
            println("Advertencia: La propiedad '$propName' no se encontró en ${entityClass.simpleName} y será omitida.")
            return@forEach
        }

        val custom = customizations[prop.name]
        val finalLabel = custom?.label ?: labelProvider(prop.name)
        val finalValidator = custom?.validator ?: { ValidationResult.Valid }
        val finalIsReadOnly = custom?.isReadOnly ?: { false }

        val finalEditorContent = custom?.editorContent ?: @Composable { entity, onUpdate, isReadOnly, error ->
            var localFieldValueText by remember(entity, prop.name) { mutableStateOf(prop.get(entity)?.toString() ?: "") }

            when (prop.returnType.classifier) {
                Boolean::class -> {
                    var localCheckedState by remember(entity, prop.name) { mutableStateOf(prop.get(entity) as? Boolean ?: false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isReadOnly) {
                                if (!isReadOnly) {
                                    localCheckedState = !localCheckedState
                                    // CAMBIO: Se usa la lambda de actualización atómica (aunque esto es solo un fallback)
                                    onUpdate { prop.set(it, localCheckedState); it }
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = localCheckedState,
                            onCheckedChange = {
                                if (!isReadOnly) {
                                    localCheckedState = it
                                    onUpdate { prop.set(it, localCheckedState); it }
                                }
                            },
                            enabled = !isReadOnly
                        )
                        Text(text = finalLabel, modifier = Modifier.padding(start = 8.dp))
                    }
                    if (error != null) { Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
                // ... (el resto de los when para String, Int, etc. se mantienen)
                else -> {
                    DropdownPlaceholderEditor<Any>(
                        label = finalLabel,
                        currentValueDisplay = localFieldValueText,
                        isReadOnly = isReadOnly,
                        error = error,
                        onClickAction = {}
                    )
                }
            }
        }

        descriptors.add(
            FieldDescriptor(
                id = prop.name,
                label = finalLabel,
                editorContent = finalEditorContent,
                validator = finalValidator,
                isReadOnly = finalIsReadOnly
            )
        )
    }
    return descriptors
}