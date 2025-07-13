package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.ConfiguracionViewModel
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    viewModel: ConfiguracionViewModel,
    onDismiss: () -> Unit
) {
    val configState by viewModel.configState.collectAsState()
    var currentConfig by remember { mutableStateOf(configState) }

    LaunchedEffect(configState) {
        currentConfig = configState
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración General") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    Button(onClick = {
                        viewModel.saveConfiguracion(currentConfig)
                        NotificationManager.show("Configuración guardada.", NotificationType.SUCCESS)
                        onDismiss()
                    },shape = RoundedCornerShape(5.dp)) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sincronización
            Text("Sincronización", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentConfig.tiempoDescargaMinutos.toString(),
                onValueChange = { value ->
                    currentConfig = currentConfig.copy(tiempoDescargaMinutos = value.toIntOrNull() ?: 0)
                },
                label = { Text("Intervalo de descarga de datos (minutos)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentConfig.tiempoSubidaMinutos.toString(),
                onValueChange = { value ->
                    currentConfig = currentConfig.copy(tiempoSubidaMinutos = value.toIntOrNull() ?: 0)
                },
                label = { Text("Intervalo de subida de datos (minutos)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Código de Barras
            Text("Código de Barras de Balanza", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currentConfig.codigoBarra.inicio.toString(),
                    onValueChange = { value ->
                        currentConfig = currentConfig.copy(codigoBarra = currentConfig.codigoBarra.copy(inicio = value.toIntOrNull() ?: 0))
                    },
                    label = { Text("Inicio") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = currentConfig.codigoBarra.long.toString(),
                    onValueChange = { value ->
                        currentConfig = currentConfig.copy(codigoBarra = currentConfig.codigoBarra.copy(long = value.toIntOrNull() ?: 0))
                    },
                    label = { Text("Longitud") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentConfig.codigoBarra.id_long.toString(),
                onValueChange = { value ->
                    currentConfig = currentConfig.copy(codigoBarra = currentConfig.codigoBarra.copy(id_long = value.toIntOrNull() ?: 0))
                },
                label = { Text("Longitud de ID") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentConfig.codigoBarra.payload_type,
                onValueChange = { value ->
                    currentConfig = currentConfig.copy(codigoBarra = currentConfig.codigoBarra.copy(payload_type = value))
                },
                label = { Text("Tipo de Payload (ej: P para peso, I para importe)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentConfig.codigoBarra.payload_int.toString(),
                onValueChange = { value ->
                    currentConfig = currentConfig.copy(codigoBarra = currentConfig.codigoBarra.copy(payload_int = value.toIntOrNull() ?: 0))
                },
                label = { Text("Enteros del Payload") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )


            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Balanza
            Text("Balanza", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentConfig.balanza.puerto,
                onValueChange = { value ->
                    currentConfig = currentConfig.copy(balanza = currentConfig.balanza.copy(puerto = value))
                },
                label = { Text("Puerto (ej: COM1)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentConfig.balanza.baudios.toString(),
                onValueChange = { value ->
                    currentConfig = currentConfig.copy(balanza = currentConfig.balanza.copy(baudios = value.toIntOrNull() ?: 0))
                },
                label = { Text("Baudios") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentConfig.balanza.reintentos.toString(),
                onValueChange = { value ->
                    currentConfig = currentConfig.copy(balanza = currentConfig.balanza.copy(reintentos = value.toIntOrNull() ?: 0))
                },
                label = { Text("Cantidad de Intentos") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}