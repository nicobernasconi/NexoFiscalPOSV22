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
import ar.com.nexofiscal.nexofiscalposv2.managers.SessionManager
import ar.com.nexofiscal.nexofiscalposv2.network.ApiClient
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedConfigScreen(onDismiss: () -> Unit) {
    var baseUrl by remember { mutableStateOf(SessionManager.getApiBaseUrl()) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var tiempoDescarga by remember { mutableStateOf(SessionManager.getTiempoDescargaMin().toString()) }
    var tiempoSubida by remember { mutableStateOf(SessionManager.getTiempoSubidaMin().toString()) }
    var maxBackups by remember { mutableStateOf(SessionManager.getMaxBackups().toString()) }
    var syncMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración Avanzada") },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        SessionManager.setApiBaseUrl(baseUrl.trim())
                        ApiClient.refreshBaseUrl()
                        val d = tiempoDescarga.toIntOrNull()
                        val s = tiempoSubida.toIntOrNull()
                        val m = maxBackups.toIntOrNull()
                        if (d == null || d <= 0 || s == null || s <= 0 || m == null || m < 1) {
                            NotificationManager.show("Valores inválidos", NotificationType.ERROR)
                        } else {
                            SessionManager.setTiempoDescargaMin(d)
                            SessionManager.setTiempoSubidaMin(s)
                            SessionManager.setMaxBackups(m)
                            NotificationManager.show("Configuración avanzada guardada.", NotificationType.SUCCESS)
                            onDismiss()
                        }
                    }, shape = RoundedCornerShape(5.dp)) { Text("Guardar") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- URL BASE ---
            Text("API", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("URL Base API") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    SessionManager.resetApiBaseUrl()
                    baseUrl = SessionManager.getApiBaseUrl()
                    ApiClient.refreshBaseUrl()
                    NotificationManager.show("URL Base restablecida por defecto.", NotificationType.INFO)
                }, shape = RoundedCornerShape(5.dp)) { Text("Reset") }
                Button(onClick = {
                    val isValid = baseUrl.startsWith("http://") || baseUrl.startsWith("https://")
                    testResult = if (isValid) {
                        "Formato válido (no se realizó llamada)."
                    } else {
                        "Formato inválido. Debe iniciar con http:// o https://"
                    }
                }, shape = RoundedCornerShape(5.dp)) { Text("Probar Formato") }
            }
            testResult?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            HorizontalDivider()

            // --- TIEMPOS DE SINCRONIZACIÓN ---
            Text("Sincronización", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = tiempoDescarga,
                onValueChange = { tiempoDescarga = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Tiempo Descarga (min)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = tiempoSubida,
                onValueChange = { tiempoSubida = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Tiempo Subida (min)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = maxBackups,
                onValueChange = { maxBackups = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Cantidad Máxima de Backups") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val d = tiempoDescarga.toIntOrNull()
                    val s = tiempoSubida.toIntOrNull()
                    val m = maxBackups.toIntOrNull()
                    if (d == null || d <= 0 || s == null || s <= 0 || m == null || m < 1) {
                        syncMsg = "Valores inválidos"
                        NotificationManager.show("Valores inválidos", NotificationType.ERROR)
                    } else {
                        SessionManager.setTiempoDescargaMin(d)
                        SessionManager.setTiempoSubidaMin(s)
                        SessionManager.setMaxBackups(m)
                        syncMsg = "Valores aplicados"
                        NotificationManager.show("Valores aplicados", NotificationType.SUCCESS)
                    }
                }, shape = RoundedCornerShape(5.dp)) { Text("Aplicar") }
                OutlinedButton(onClick = {
                    tiempoDescarga = SessionManager.getTiempoDescargaMin().toString()
                    tiempoSubida = SessionManager.getTiempoSubidaMin().toString()
                    maxBackups = SessionManager.getMaxBackups().toString()
                    syncMsg = "Recargados"
                }, shape = RoundedCornerShape(5.dp)) { Text("Recargar") }
            }
            syncMsg?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

            HorizontalDivider()
            Text(
                "Nota: Al superar la cantidad máxima se elimina el backup más antiguo automáticamente.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
