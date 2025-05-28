// FormScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 1) Definimos los tipos de campo posibles
sealed class FormField(val key: String, val label: String) {
    data class Text(
        val fieldKey: String,
        val fieldLabel: String,
        val initial: String = ""
    ) : FormField(fieldKey, fieldLabel)

    data class Number(
        val fieldKey: String,
        val fieldLabel: String,
        val initial: String = ""
    ) : FormField(fieldKey, fieldLabel)

    data class Dropdown(
        val fieldKey: String,
        val fieldLabel: String,
        val options: List<String>,
        val initial: String? = null
    ) : FormField(fieldKey, fieldLabel)

    data class Radio(
        val fieldKey: String,
        val fieldLabel: String,
        val options: List<String>,
        val initial: String? = null
    ) : FormField(fieldKey, fieldLabel)

    data class Checkbox(
        val fieldKey: String,
        val fieldLabel: String,
        val initial: Boolean = false
    ) : FormField(fieldKey, fieldLabel)
}

// 2) Composable genérico de formulario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FormScreen(
    title: String,
    isCreate: Boolean,
    fields: List<FormField>,
    mapper: (Map<String, Any?>) -> T,
    onSubmit: (T) -> Unit,
    onCancel: () -> Unit = {}
) {
    // Estado interno: mapa clave→valor
    val values = remember {
        mutableStateMapOf<String, Any?>().apply {
            fields.forEach { field ->
                put(field.key, when (field) {
                    is FormField.Text     -> field.initial
                    is FormField.Number   -> field.initial
                    is FormField.Dropdown -> field.initial
                    is FormField.Radio    -> field.initial
                    is FormField.Checkbox -> field.initial
                })
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "${if (isCreate) "Crear" else "Editar"} $title")
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Por cada campo, renderizamos el control adecuado
            fields.forEach { field ->
                Spacer(Modifier.height(12.dp))
                when (field) {
                    is FormField.Text -> {
                        var text by remember { mutableStateOf(values[field.key] as String) }
                        OutlinedTextField(
                            value = text,
                            onValueChange = {
                                text = it
                                values[field.key] = it
                            },
                            label = { Text(field.label) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is FormField.Number -> {
                        var number by remember { mutableStateOf(values[field.key] as String) }
                        OutlinedTextField(
                            value = number,
                            onValueChange = { input ->
                                // filtrado manual: sólo dígitos y punto
                                val filtered = input.filter { it.isDigit() || it == '.' }
                                number = filtered
                                values[field.key] = filtered
                            },
                            label = { Text(field.label) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is FormField.Dropdown -> {
                        var expanded by remember { mutableStateOf(false) }
                        var selected by remember { mutableStateOf(values[field.key] as String? ?: "") }
                        Box {
                            OutlinedTextField(
                                value = selected,
                                onValueChange = {},
                                label = { Text(field.label) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = true },
                                readOnly = true
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                field.options.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selected = option
                                            values[field.key] = option
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is FormField.Radio -> {
                        Text(field.label, style = MaterialTheme.typography.labelLarge)
                        var choice by remember { mutableStateOf(values[field.key] as String? ?: "") }
                        field.options.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = (choice == option),
                                    onClick = {
                                        choice = option
                                        values[field.key] = option
                                    }
                                )
                                Text(option, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                    is FormField.Checkbox -> {
                        var checked by remember { mutableStateOf(values[field.key] as Boolean) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    checked = it
                                    values[field.key] = it
                                }
                            )
                            Text(field.label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botones Cancelar / Enviar
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancelar")
                }
                Spacer(Modifier.width(16.dp))
                Button(onClick = { onSubmit(mapper(values)) }) {
                    Text(if (isCreate) "Crear" else "Guardar")
                }
            }
        }
    }
}
