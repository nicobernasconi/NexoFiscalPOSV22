// main/java/ar/com/nexofiscal/nexofiscalposv2/screens/edit/EntityEditScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.AzulNexo
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.Blanco
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.NegroNexo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EntityEditScreen(
    title: String,
    initialEntity: T,
    fieldDescriptors: List<FieldDescriptor<T>>,
    onSave: (T) -> Unit,
    onCancel: () -> Unit
) {
    var editableEntity by remember(initialEntity) { mutableStateOf(initialEntity) }
    val validationErrors = remember { mutableStateMapOf<String, String?>() }

    fun validateAllFields(entityToValidate: T): Boolean {
        var allValid = true
        validationErrors.clear()
        fieldDescriptors.forEach { descriptor ->
            when (val result = descriptor.validator(entityToValidate)) {
                is ValidationResult.Invalid -> {
                    validationErrors[descriptor.id] = result.errorMessage
                    allValid = false
                }
                else -> { /* No hacer nada en caso válido */ }
            }
        }
        return allValid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.edit_entity_dialog_cancel))
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (validateAllFields(editableEntity)) {
                                onSave(editableEntity)
                            }
                        },
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NegroNexo),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
                        modifier = Modifier.padding(end = 2.dp, top = 2.dp, bottom = 2.dp)
                    ) {
                        Icon(Icons.Default.Save, stringResource(R.string.edit_entity_dialog_save), tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulNexo,
                    titleContentColor = Blanco,
                    navigationIconContentColor = Blanco
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
           items(fieldDescriptors, key = { it.id }) { descriptor ->
                           descriptor.editorContent(
                               editableEntity,
                               { updateAction ->
                                   editableEntity = updateAction(editableEntity)
                               },
                               descriptor.isReadOnly?.invoke(editableEntity) ?: false,
                               validationErrors[descriptor.id]
                           )
                           HorizontalDivider(modifier = Modifier.padding(top = 16.dp), thickness = 0.5.dp)
                       }

        }
    }
}