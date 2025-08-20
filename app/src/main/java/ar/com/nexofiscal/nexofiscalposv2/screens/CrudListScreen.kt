package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.AzulNexo
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.Blanco
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.GrisClaro
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.NegroNexo
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.RojoError

enum class CrudScreenMode {
    VIEW_SELECT,
    EDIT_DELETE,
    EDIT_DELETE_EDIT_PRINT,
    ONLY_VIEW
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun <T: Any> CrudListScreen(
    title: String,
    items: LazyPagingItems<T>,
    itemContent: @Composable (T) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    onAttemptEdit: ((T) -> Unit)? = null,
    onAttemptDelete: ((T) -> Unit)? = null, // Renombrado de onDelete
    useInternalDeleteDialog: Boolean = true,
    onCreate: (() -> Unit)? = null,
    isActionEnabled: ((T) -> Boolean)? = null,
    screenMode: CrudScreenMode = CrudScreenMode.VIEW_SELECT,
    itemKey: ((T) -> Any)? = null,
    searchHint: String = stringResource(R.string.default_search_hint)
) {
    LaunchedEffect(Unit) {
        onSearchQueryChanged("")
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var searchText by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<T?>(null) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.clip(RoundedCornerShape(5.dp))) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Blanco)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulNexo,
                    titleContentColor = Blanco,
                    navigationIconContentColor = Blanco
                )
            )
        },
        floatingActionButton = {
            if (onCreate != null && screenMode != CrudScreenMode.VIEW_SELECT && screenMode != CrudScreenMode.ONLY_VIEW) {
                FloatingActionButton(
                    onClick = onCreate,
                    containerColor = AzulNexo,
                    contentColor = Blanco
                ) {
                    Icon(Icons.Default.Add, stringResource(R.string.create_new_item), tint = Blanco)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    onSearchQueryChanged(it)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(searchHint) },
                leadingIcon = { Icon(Icons.Default.Search, stringResource(R.string.search_icon_description)) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = {
                            searchText = ""
                            onSearchQueryChanged("")
                        }, modifier = Modifier.clip(RoundedCornerShape(5.dp))) {
                            Icon(Icons.Default.Clear, stringResource(R.string.clear_search_description))
                        }
                    }
                },
                singleLine = true
            )

            when (items.loadState.refresh) {
                is LoadState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is LoadState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error al cargar los datos.")
                    }
                }
                else -> {
                    if (items.itemCount == 0) {
                        EmptyStateOrNoResults(modifier = Modifier.weight(1f), isSearchActive = searchText.isNotBlank())
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.background),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(
                                count = items.itemCount,
                                key = if (itemKey != null) { index -> items.peek(index)?.let { itemKey(it) } ?: index } else null
                            ) { index ->
                                val item = items[index]
                                if (item != null) {
                                    CrudListItem(
                                        item = item,
                                        itemContent = { itemContent(item) },
                                        onSelect = { onSelect(item) },
                                        onEdit = onAttemptEdit,
                                         onDelete  = { selectedItem ->
                                            if (useInternalDeleteDialog) {
                                                itemToDelete = selectedItem
                                                showDeleteDialog = true
                                            } else {
                                                // Si el diálogo interno está desactivado, llamamos directamente a la acción.
                                                onAttemptDelete?.invoke(selectedItem)
                                            }
                                        },
                                        isActionEnabled = isActionEnabled?.invoke(item) ?: true,
                                        showSelectActionIcon = screenMode == CrudScreenMode.VIEW_SELECT,
                                        screenMode = screenMode
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                itemToDelete = null
            },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar este elemento? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let {
                            onAttemptDelete?.invoke(it)
                        }
                        showDeleteDialog = false
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RojoError),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    itemToDelete = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// --- INICIO DE LA MODIFICACIÓN ---
@Composable
fun <T> CrudListItem(
    item: T,
    itemContent: @Composable () -> Unit,
    onSelect: () -> Unit,
    onEdit: ((T) -> Unit)?,
    onDelete: ((T) -> Unit)?,
    isActionEnabled: Boolean,
    showSelectActionIcon: Boolean,
    screenMode: CrudScreenMode
) {
    val backgroundColor = if (isActionEnabled) GrisClaro else GrisClaro.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) { itemContent() }
            Spacer(modifier = Modifier.width(8.dp))
            if (screenMode == CrudScreenMode.VIEW_SELECT) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Seleccionar",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                if (onEdit != null) {
                    IconButton(onClick = { onEdit(item) }, enabled = isActionEnabled, modifier = Modifier.clip(RoundedCornerShape(5.dp))) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = { onDelete(item) }, enabled = isActionEnabled, modifier = Modifier.clip(RoundedCornerShape(5.dp))) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = RojoError)
                    }
                }
            }
        }
    }
}
// --- FIN DE LA MODIFICACIÓN ---

@Composable
fun EmptyStateOrNoResults(modifier: Modifier = Modifier, isSearchActive: Boolean) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isSearchActive) stringResource(R.string.no_search_results)
            else stringResource(R.string.no_items_to_display),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}