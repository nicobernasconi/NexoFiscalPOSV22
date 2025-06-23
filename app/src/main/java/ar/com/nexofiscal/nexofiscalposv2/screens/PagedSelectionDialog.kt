package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.paging.compose.LazyPagingItems

/**
 * Un diálogo genérico que muestra una lista paginada de items para su selección.
 * Reutiliza CrudListScreen en un modo de solo vista/selección.
 *
 * @param T El tipo de entidad a mostrar.
 * @param showDialog Controla la visibilidad del diálogo.
 * @param onDismiss Callback para cuando el diálogo se cierra.
 * @param title Título del diálogo.
 * @param items Los datos paginados a mostrar.
 * @param itemLabel Función para obtener el texto a mostrar por cada item.
 * @param onSearch Callback que se activa cuando el usuario escribe en la barra de búsqueda.
 * @param onSelect Callback que se activa cuando el usuario selecciona un item de la lista.
 */
@Composable
fun <T : Any> PagedSelectionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    title: String,
    items: LazyPagingItems<T>,
    itemContent: @Composable (T) -> Unit,
    onSearch: (String) -> Unit,
    onSelect: (T) -> Unit
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.85f),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                // Reutilizamos CrudListScreen, configurado para selección.
                CrudListScreen(
                    title = title,
                    items = items,
                    itemContent = itemContent,
                    onSearchQueryChanged = onSearch,
                    onSelect = onSelect, // La acción principal al hacer clic en un item
                    onDismiss = onDismiss,
                    // Usamos ONLY_VIEW para una lista de selección limpia, sin íconos de acción.
                    screenMode = CrudScreenMode.ONLY_VIEW
                )
            }
        }
    }
}