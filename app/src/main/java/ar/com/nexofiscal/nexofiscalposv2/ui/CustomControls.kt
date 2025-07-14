// main/java/ar/com/nexofiscal/nexofiscalposv2/ui/CustomControls.kt
package ar.com.nexofiscal.nexofiscalposv2.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.paging.compose.LazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.screens.CrudListScreen
import ar.com.nexofiscal.nexofiscalposv2.screens.CrudScreenMode
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.BordeSuave

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
                screenMode = CrudScreenMode.VIEW_SELECT,
                itemKey = itemKey
            )
        }
    }
}

/**
 * Componente de texto corregido y robusto que soluciona el problema del cursor
 * y la entrada de números.
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
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    // Sincroniza el estado interno con el valor externo cuando este cambia.
    // Esto es crucial y ahora está hecho de forma que no interfiere con la escritura del usuario.
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = textFieldValue.copy(text = value)
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValue ->
            // Se actualiza el estado interno y se notifica al ViewModel.
            // Este es el flujo unidireccional de datos correcto.
            textFieldValue = newTextFieldValue
            if (value != newTextFieldValue.text) {
                onValueChange(newTextFieldValue.text)
            }
        },
        label = { Text(label) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        readOnly = isReadOnly,
        keyboardOptions = keyboardOptions,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    // Selecciona todo el texto solo cuando el campo gana el foco.
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(0, textFieldValue.text.length)
                    )
                }
            }
    )
}