// src/main/java/ar/com/nexofiscal/nexofiscalposv2/screens/ClientesListScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

// ... otras importaciones ...
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel // Asegúrate de esta importación
import ar.com.nexofiscal.nexofiscalposv2.managers.ClienteManager
import ar.com.nexofiscal.nexofiscalposv2.models.Cliente
import ar.com.nexofiscal.nexofiscalposv2.ui.LoadingDialog
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationHost
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import ar.com.nexofiscal.nexofiscalposv2.ui.viewmodel.ClienteViewModel // ViewModel de Room
import ar.com.nexofiscal.nexofiscalposv2.db.mappers.toClienteEntityList // Tu mapper

@Composable
fun ClientesListScreen(
    onEdit: (Cliente) -> Unit,
    onSelect: (Cliente) -> Unit,
    onDelete: (Cliente) -> Unit,
    tipo: String = "R"
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("nexofiscal", Context.MODE_PRIVATE)
    val token = prefs.getString("token", "") ?: ""

    // ViewModel de Room para Clientes
    val clienteViewModel: ClienteViewModel = viewModel {
        ClienteViewModel(context.applicationContext)
    }

    val clientesApi = remember { mutableStateListOf<Cliente>() } // Lista para los modelos de la API
    var isLoading by remember { mutableStateOf(true) }

    val headers = remember { // Usar remember para que no se recree en cada recomposición
        mutableMapOf<String?, String?>().apply {
            put("Authorization", "Bearer $token")
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        ClienteManager.obtenerClientes(headers, object : ClienteManager.ClienteListCallback {
            override fun onSuccess(list: MutableList<Cliente?>?) {
                val clientesObtenidos = list?.filterNotNull() ?: emptyList()
                clientesApi.clear()
                clientesApi.addAll(clientesObtenidos)

                // Guardar en Room DB
                val clienteEntities = clientesObtenidos.toClienteEntityList()
                clienteEntities.forEach { entity ->
                    clienteViewModel.save(entity)
                }


                isLoading = false
                NotificationManager.show("Clientes actualizados localmente.", NotificationType.SUCCESS)
            }

            override fun onError(errorMessage: String?) {
                isLoading = false
                NotificationManager.show(
                    message = errorMessage ?: "Error cargando clientes",
                    type = NotificationType.ERROR
                )
            }
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CrudListScreen(
            title = "Listado de Clientes",
            items = clientesApi, // Mostrar los datos de la API o podrías observar los de Room con clienteViewModel.clientes.collectAsState()
            itemLabel = { "${it.nombre} (${it.cuit})" },
            onEdit = onEdit,
            onSelect = onSelect,
            onDelete = onDelete,
            type = tipo
        )
        LoadingDialog(
            show = isLoading,
            message = "Cargando clientes..."
        )
        NotificationHost()
    }
}