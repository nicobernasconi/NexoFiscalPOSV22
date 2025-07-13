package ar.com.nexofiscal.nexofiscalposv2.screens.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import ar.com.nexofiscal.nexofiscalposv2.models.Agrupacion
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.FieldDescriptor
import ar.com.nexofiscal.nexofiscalposv2.screens.edit.ValidationResult
import ar.com.nexofiscal.nexofiscalposv2.ui.ColorPickerDialog
import ar.com.nexofiscal.nexofiscalposv2.ui.ColorSelectionField
import ar.com.nexofiscal.nexofiscalposv2.ui.SelectAllTextField

fun getAgrupacionFieldDescriptors(): List<FieldDescriptor<Agrupacion>> {
    return listOf(
        FieldDescriptor(
            id = "nombre",
            label = "Nombre",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // Reemplazamos OutlinedTextField por nuestro control personalizado
                SelectAllTextField(
                    value = entity.nombre ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(nombre = newValue) } },
                    label = "Nombre de la Agrupación",
                    isReadOnly = isReadOnly,
                    error = error
                )
            },
            validator = { entity ->
                if (entity.nombre.isNullOrBlank()) {
                    ValidationResult.Invalid("El nombre es obligatorio.")
                } else {
                    ValidationResult.Valid
                }
            }
        ),
        FieldDescriptor(
            id = "numero",
            label = "Número",
            editorContent = { entity, onUpdate, isReadOnly, error ->
                // Usamos SelectAllTextField también para el campo numérico
                SelectAllTextField(
                    value = entity.numero?.toString() ?: "",
                    onValueChange = { newValue -> onUpdate { it.copy(numero = newValue.toIntOrNull()) } },
                    label = "Número (opcional)",
                    isReadOnly = isReadOnly,
                    error = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        ),
        FieldDescriptor(
            id = "color",
            label = "Color",
            editorContent = { entity, onUpdate, isReadOnly, _ ->
                var showColorPicker by remember { mutableStateOf(false) }

                // El control de selección de color que ya habías implementado
                ColorSelectionField(
                    label = "Color de Agrupación",
                    hexColor = entity.color,
                    isReadOnly = isReadOnly,
                    onClick = {
                        if (!isReadOnly) {
                            showColorPicker = true
                        }
                    }
                )

                if (showColorPicker) {
                    ColorPickerDialog(
                        initialColor = entity.color,
                        onDismiss = { showColorPicker = false },
                        onColorSelected = { newColor ->
                            // Aplicamos el patrón de actualización atómica
                            onUpdate { it.copy(color = newColor) }
                            showColorPicker = false
                        }
                    )
                }
            }
        )
    )
}