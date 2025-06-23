package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.com.nexofiscal.nexofiscalposv2.managers.SyncProgress
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.AzulNexo

@Composable
fun SyncStatusScreen(
    progress: SyncProgress,
    onFinish: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sincronizando Datos",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Barra de progreso general
            LinearProgressIndicator(
                progress = { progress.overallTaskIndex.toFloat() / progress.totalTasks },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Texto de estado
            Text(
                text = "Paso ${progress.overallTaskIndex} de ${progress.totalTasks}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = progress.currentTaskName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Registros descargados: ${progress.currentTaskItemCount}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lista de errores
            if (progress.errors.isNotEmpty()) {
                Text(
                    "Errores encontrados:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(top = 8.dp)
                ) {
                    items(progress.errors) { error ->
                        ListItem(
                            headlineContent = { Text(error, fontSize = 12.sp) },
                            leadingContent = { Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón para finalizar
            Button(
                onClick = onFinish,
                enabled = progress.isFinished,
                modifier = Modifier.fillMaxWidth(), shape= RoundedCornerShape(5.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = AzulNexo,
                    contentColor = Color.White
                )
            ) {
                Text(if (progress.isFinished) "FINALIZAR" else "ESPERE...")
            }
        }
    }
}