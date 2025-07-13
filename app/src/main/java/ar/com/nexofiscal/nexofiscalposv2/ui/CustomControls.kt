// main/java/ar/com/nexofiscal/nexofiscalposv2/ui/CustomControls.kt

package ar.com.nexofiscal.nexofiscalposv2.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.paging.compose.LazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.screens.CrudListScreen
import ar.com.nexofiscal.nexofiscalposv2.screens.CrudScreenMode
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.BordeSuave

/**
 * Un botón personalizado diseñado para seleccionar una entidad relacionada.
 * Muestra una etiqueta, el nombre del valor actual y un ícono.
 * Al hacer clic, ejecuta la acción `onClick`.
 */
@Composable
fun EntitySelectionButton(
    label: String,
    selectedValue: String?,
    isReadOnly: Boolean,
    error: String?,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    shape = BordeSuave
                )
                .clickable(enabled = !isReadOnly, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (selectedValue.isNullOrBlank()) "Seleccionar..." else selectedValue,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedValue.isNullOrBlank()) Color.Gray else MaterialTheme.colorScheme.onSurface
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_drop_down24),
                contentDescription = "Seleccionar",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Un Composable de ayuda para crear una fila con un Checkbox y un texto,
 * simplificando la creación de campos booleanos.
 */
@Composable
fun CheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, isReadOnly: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isReadOnly) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = if (isReadOnly) null else onCheckedChange,
            enabled = !isReadOnly
        )
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}

/**
 * Un componente genérico que envuelve CrudListScreen en un Dialog para selección.
 */
@Composable
fun <T : Any> SelectionModal(
    title: String,
    pagedItems: LazyPagingItems<T>,
    itemContent: @Composable (T) -> Unit,
    onSearch: (String) -> Unit,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    itemKey: ((T) -> Any)? = null,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            Modifier.fillMaxSize(0.9f),
            shape = MaterialTheme.shapes.large
        ) {
            CrudListScreen(
                title = title,
                items = pagedItems,
                itemContent = itemContent,
                onSearchQueryChanged = onSearch,
                onSelect = onSelect,
                onDismiss = onDismiss,
                screenMode = CrudScreenMode.VIEW_SELECT, // ¡Modo selección!
                itemKey = itemKey
            )
        }
    }
}

/**
 * Un OutlinedTextField personalizado que selecciona todo su contenido
 * cuando recibe el foco.
 */
@Composable
fun SelectAllTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange.Zero))
    }
    var isFocused by remember { mutableStateOf(false) }

    // Este efecto se asegura de que si el valor externo cambia (por ejemplo, al cargar la entidad),
    // el estado interno del campo de texto se actualice.
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = textFieldValue.copy(text = value)
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            // Se actualiza el estado interno y se notifica hacia afuera solo el string.
            textFieldValue = it
            onValueChange(it.text)
        },
        label = { Text(label) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        readOnly = isReadOnly,
        keyboardOptions = keyboardOptions,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (isFocused) {
                    // ¡La magia sucede aquí! Al hacer foco, se selecciona todo el texto.
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(0, textFieldValue.text.length)
                    )
                }
            }
    )
}

/**
 * Componente de Texto reutilizable que selecciona todo su contenido al hacer foco.
 */
@Composable
 fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isReadOnly: Boolean,
    error: String?
) {
    var textFieldValue by remember(value) { mutableStateOf(TextFieldValue(value)) }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            if (value != it.text) {
                onValueChange(it.text)
            }
        },
        label = { Text(label) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        readOnly = isReadOnly,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(0, textFieldValue.text.length)
                    )
                }
            }
    )
}

/**
 * Campo de texto NUMÉRICO que selecciona todo al hacer foco y solo actualiza el modelo
 * cuando el campo pierde el foco, para una edición fluida.
 */
@Composable
fun SelectAllNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false,
    error: String? = null
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    var isFocused by remember { mutableStateOf(false) }

    // Sincroniza el estado interno si el valor externo cambia, pero SOLO si el campo no está en foco.
    LaunchedEffect(value) {
        if (!isFocused) {
            val formattedValue = value.toDoubleOrNull()?.toString() ?: "0.0"
            if (textFieldValue.text != formattedValue) {
                textFieldValue = textFieldValue.copy(text = formattedValue)
            }
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            // Permite solo números y un punto decimal, actualizando solo el estado local.
            if (it.text.matches(Regex("^\\d*\\.?\\d*$"))) {
                textFieldValue = it
            }
        },
        label = { Text(label) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        readOnly = isReadOnly,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    textFieldValue = textFieldValue.copy(selection = TextRange(0, textFieldValue.text.length))
                } else {
                    // Al perder el foco, se notifica el cambio final al modelo.
                    onValueChange(textFieldValue.text)
                }
            }
    )
}