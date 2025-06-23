package ar.com.nexofiscal.nexofiscalposv2.screens

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ar.com.nexofiscal.nexofiscalposv2.AdminReceiver
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KioskConfigScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    // Verificamos si la app es dueña del dispositivo para saber qué modo se activará
    val isDeviceOwner = dpm.isDeviceOwnerApp(context.packageName)

    val prefs = remember { context.getSharedPreferences("nexofiscal", Context.MODE_PRIVATE) }
    var isKioskModeEnabled by remember { mutableStateOf(prefs.getBoolean("kiosk_mode_enabled", false)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Modo Kiosco") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            // --- TEXTO INFORMATIVO MODIFICADO ---
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Modo Kiosco / Fijar Pantalla", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (isDeviceOwner) {
                            "Su dispositivo está configurado correctamente. Al activar esta opción, la aplicación entrará en Modo Kiosco (Lock Task Mode), un bloqueo de alta seguridad."
                        } else {
                            "Al activar esta opción, la aplicación se fijará en la pantalla. Esto previene salidas accidentales. Para salir de este modo, el usuario debe mantener presionados los botones 'Atrás' y 'Recientes' simultáneamente."
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isDeviceOwner) "Activar Modo Kiosco" else "Activar Fijar Pantalla",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isKioskModeEnabled,
                    onCheckedChange = { isEnabled ->
                        isKioskModeEnabled = isEnabled
                        prefs.edit().putBoolean("kiosk_mode_enabled", isEnabled).apply()
                        try {
                            if (isEnabled) {
                                activity?.startLockTask()
                                NotificationManager.show("Modo Kiosco Activado.", NotificationType.SUCCESS)
                            } else {
                                activity?.stopLockTask()
                                NotificationManager.show("Modo Kiosco Desactivado.", NotificationType.INFO)
                            }
                        } catch (e: Exception) {
                            NotificationManager.show("Error al cambiar el modo: ${e.message}", NotificationType.ERROR)
                        }
                    }
                )
            }
            Text(
                "Impide salir de la aplicación y deshabilita los botones de navegación del sistema. Se requiere reiniciar la app para aplicar en todas las pantallas.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}