// Archivo: ar/com/nexofiscal/nexofiscalposv2/screens/edit/AutomaticFieldDescriptorGenerator.kt
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
    val editorContent: (@Composable (
        entity: T,
        onUpdate: (updatedEntity: T) -> Unit, // El onUpdate aquí debería ser capaz de manejar la propiedad específica
        isReadOnly: Boolean,
        error: String?
    ) -> Unit)? = null,
    val validator: ((entity: T) -> ValidationResult)? = null,
    val isReadOnly: ((entity: T) -> Boolean)? = null,
    val keyboardTypeOverride: KeyboardType? = null // Para OutlinedTextField genérico
)

// Para referencia, incluyo DropdownPlaceholderEditor aquí si no lo tienes en otro lado accesible:
@Composable
fun <T> DropdownPlaceholderEditor( // Genérico para poder usarlo en FieldCustomization si es necesario
    label: String,
    currentValueDisplay: String, // Lo que se muestra en el campo
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
        readOnly = true, // Es un placeholder, la selección se hace por otros medios
        isError = error != null,
        supportingText = {
            if (error != null) Text(error)
            else Text("Seleccionar...") // Indicador genérico
        },
        // Puedes añadir un trailingIcon si quieres, ej. Icon(Icons.Filled.ArrowDropDown, "...")
    )
}


/**
 * Genera FieldDescriptors automáticamente para una entidad T, procesando solo las propiedades
 * especificadas en 'attributesToIncludeAndOrder'.
 * Permite personalizaciones para campos específicos.
 */
inline fun <reified T : Any> generateAutomaticFieldDescriptors(
    entityClass: KClass<T>,
    attributesToIncludeAndOrder: List<String>, // Lista de nombres de propiedades a incluir y su orden
    customizations: Map<String, FieldCustomization<T>> = emptyMap(),
    noinline labelProvider: (propName: String) -> String = { propName -> propName.replaceFirstChar { it.uppercaseChar() } }
): List<FieldDescriptor<T>> {

    val allMutableProperties = entityClass.memberProperties
        .filterIsInstance<KMutableProperty1<T, Any?>>()
        .associateBy { it.name }

    val descriptors = mutableListOf<FieldDescriptor<T>>()

    // Procesar solo las propiedades en attributesToIncludeAndOrder, y en ese orden
    attributesToIncludeAndOrder.forEach { propName ->
        val prop = allMutableProperties[propName]
        if (prop == null) {
            println("Advertencia: La propiedad '$propName' no se encontró o no es mutable en la entidad ${entityClass.simpleName} y será omitida.")
            return@forEach // Continuar con la siguiente propiedad en la lista
        }

        val custom = customizations[prop.name]
        val finalLabel = custom?.label ?: labelProvider(prop.name)
        val finalValidator = custom?.validator ?: { ValidationResult.Valid }
        val finalIsReadOnly = custom?.isReadOnly ?: { false }

        val finalEditorContent = custom?.editorContent ?: @Composable { entity, onUpdate, isReadOnly, error ->
            // Este editor por defecto es principalmente para la UI y el estado local.
            // La lógica de onUpdate(entity.copy(...)) DEBE ser proporcionada
            // en 'customizations' para una actualización funcional de la entidad.
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
                                    // ¡IMPORTANTE! Llamar a onUpdate con la entidad actualizada
                                    // requiere que 'customizations' provea un editorContent que sepa
                                    // cómo construir la nueva entidad T.
                                    // Ejemplo: onUpdate(entity.copy(prop.name = localCheckedState))
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = localCheckedState,
                            onCheckedChange = {
                                if(!isReadOnly) {
                                    localCheckedState = it
                                    // Ver nota arriba sobre onUpdate
                                }
                            },
                            enabled = !isReadOnly
                        )
                        Text(text = finalLabel, modifier = Modifier.padding(start = 8.dp))
                    }
                    if (error != null) { Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
                String::class -> {
                    OutlinedTextField(
                        value = localFieldValueText,
                        onValueChange = { localFieldValueText = it /* Actualiza estado local */ },
                        label = { Text(finalLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = isReadOnly,
                        isError = error != null,
                        supportingText = { if (error != null) Text(error) },
                        keyboardOptions = KeyboardOptions(keyboardType = custom?.keyboardTypeOverride ?: KeyboardType.Text)
                    )
                }
                Int::class, Long::class -> {
                    OutlinedTextField(
                        value = localFieldValueText,
                        onValueChange = { localFieldValueText = it /* Actualiza estado local */ },
                        label = { Text(finalLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = isReadOnly,
                        isError = error != null,
                        supportingText = { if (error != null) Text(error) },
                        keyboardOptions = KeyboardOptions(keyboardType = custom?.keyboardTypeOverride ?: KeyboardType.Number)
                    )
                }
                Double::class, Float::class -> {
                    OutlinedTextField(
                        value = localFieldValueText,
                        onValueChange = { localFieldValueText = it /* Actualiza estado local */ },
                        label = { Text(finalLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = isReadOnly,
                        isError = error != null,
                        supportingText = { if (error != null) Text(error) },
                        keyboardOptions = KeyboardOptions(keyboardType = custom?.keyboardTypeOverride ?: KeyboardType.Decimal)
                    )
                }
                else -> { // Para objetos u otros tipos no primitivos/string/boolean
                   DropdownPlaceholderEditor<Any>(
                            label = finalLabel,
                            currentValueDisplay = localFieldValueText, // Muestra el toString() o "Seleccionar..."
                            isReadOnly = isReadOnly,
                            error = error,
                            onClickAction = {
                                // La lógica para mostrar el dropdown real iría en una personalización
                            }
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