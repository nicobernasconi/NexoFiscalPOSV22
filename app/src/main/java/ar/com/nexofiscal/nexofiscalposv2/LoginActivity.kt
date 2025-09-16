package ar.com.nexofiscal.nexofiscalposv2

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import ar.com.nexofiscal.nexofiscalposv2.managers.*
import ar.com.nexofiscal.nexofiscalposv2.network.LoginHelper
import ar.com.nexofiscal.nexofiscalposv2.screens.LoginScreen
import ar.com.nexofiscal.nexofiscalposv2.screens.SyncStatusScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationHost
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager.show
import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.NexoFiscalPOSV2Theme
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import ar.com.nexofiscal.nexofiscalposv2.services.BackupScheduler
import ar.com.nexofiscal.nexofiscalposv2.services.NotificacionService
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

class LoginActivity : ComponentActivity() {

    private val BASE_URL = "https://test.nexofiscaltest.com.ar/"
    private lateinit var prefs: SharedPreferences
    private val SYNC_INTERVAL_HOURS = 24L

    // Estado para bloquear la pantalla durante auto-login
    private val autoLoginInProgress = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // --- CAMBIO 1: Se inicializa el SessionManager aquí, una sola vez. ---
        SessionManager.init(this)

        // Se obtiene la instancia de SharedPreferences (se eliminó la declaración duplicada)
        prefs = getSharedPreferences("nexofiscal", Context.MODE_PRIVATE)

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (prefs.getBoolean("kiosk_mode_enabled", false)) {
            if (dpm.isLockTaskPermitted(this.packageName)) {
                startLockTask()
            }
        }
        setContent {
            NexoFiscalPOSV2Theme {
                var showSyncScreen by remember { mutableStateOf(false) }
                val syncProgress by SyncManager.progressState.collectAsState()
                val isAutoLogin by autoLoginInProgress

                Box(Modifier.fillMaxSize()) {
                    if (showSyncScreen) {
                        SyncStatusScreen(
                            progress = syncProgress,
                            onFinish = {
                                prefs.edit().putLong("last_sync_timestamp", System.currentTimeMillis()).apply()
                                navigateToMain()
                            }
                        )
                    } else {
                        LoginScreen { usuario, password ->
                            realizarLogin(usuario, password) { showSyncScreen = true }
                        }
                    }

                    NotificationHost()

                    // Overlay bloqueante durante auto login automático
                    if (!showSyncScreen && isAutoLogin) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x99000000))
                                .pointerInput(Unit) { } // consume eventos
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(16.dp))
                                Text("Iniciando sesión automática...")
                            }
                        }
                    }
                }
            }
        }
        intentarLoginAutomatico()
    }

    private fun navigateToMain(isOffline: Boolean = false) {
        // Programar backups (cada hora) y verificar inmediatamente al entrar
        BackupScheduler.scheduleHourly(applicationContext)
        BackupScheduler.runNow(applicationContext)

        // Iniciar el servicio de notificaciones (usa DB local; aplica también en offline)
        startService(Intent(this, NotificacionService::class.java))

        // Iniciar el servicio en segundo plano si no estamos en modo offline
        if (!isOffline) {
            val serviceIntent = Intent(this, SyncService::class.java)
            startService(serviceIntent)
        }

        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.putExtra("IS_OFFLINE_MODE", isOffline)
        startActivity(intent)
        finish()
    }

    private fun intentarLoginAutomatico() {
        val savedUser = prefs.getString("saved_username", null)
        val savedPass = prefs.getString("saved_password", null)

        if (!savedUser.isNullOrBlank() && !savedPass.isNullOrBlank()) {
            Log.d("LoginActivity", "Credenciales encontradas, intentando login automático.")
            autoLoginInProgress.value = true
            realizarLogin(savedUser, savedPass) {
                autoLoginInProgress.value = false
                // Mostrar pantalla de sync (ya controlado por callback en contenido compose)
                setContent { // mantenemos lógica existente para no romper flujo previo
                    NexoFiscalPOSV2Theme {
                        val syncProgress by SyncManager.progressState.collectAsState()
                        SyncStatusScreen(
                            progress = syncProgress,
                            onFinish = {
                                prefs.edit().putLong("last_sync_timestamp", System.currentTimeMillis()).apply()
                                navigateToMain()
                            }
                        )
                        NotificationHost()
                    }
                }
            }
        }
    }

    // Nuevo helper: determina si existe una sesión previa válida para habilitar modo offline
    private fun hasPreviousSuccessfulLogin(): Boolean {
        val hasCreds = prefs.contains("saved_username") && prefs.contains("saved_password")
        val lastSync = prefs.getLong("last_sync_timestamp", 0L)
        return hasCreds && lastSync > 0L
    }

    private fun realizarLogin(usuario: String, password: String, onSyncRequired: () -> Unit) {
        LoginHelper.login(
            this,
            BASE_URL,
            usuario,
            password,
            object : LoginHelper.LoginCallback {
                override fun onSuccess(response: JSONObject?) {
                    // La lógica de guardado ya fue manejada por LoginHelper -> SessionManager
                    prefs.edit()
                        .putString("saved_username", usuario)
                        .putString("saved_password", password)
                        .apply()

                    // --- CAMBIO 2: Se obtiene el token desde el SessionManager ---
                    val tokenGuardado = SessionManager.token
                    if (tokenGuardado.isNullOrBlank()) {
                        show("Error crítico: No se pudo obtener el token de sesión.", NotificationType.ERROR)
                        autoLoginInProgress.value = false
                        return
                    }

                    val lastSync = prefs.getLong("last_sync_timestamp", 0L)
                    val hoursSinceLastSync = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastSync)

                    if (lastSync == 0L || hoursSinceLastSync >= SYNC_INTERVAL_HOURS) {
                        Log.d("LoginActivity", "Sincronización requerida. Han pasado $hoursSinceLastSync horas.")
                        onSyncRequired()
                        lifecycleScope.launch {
                            SyncManager.startFullSync(applicationContext, tokenGuardado)
                        }
                    } else {
                        Log.d("LoginActivity", "Sincronización no requerida. Saltando al MainActivity.")
                        show("Sincronización reciente. Cargando datos locales.", NotificationType.SUCCESS)
                        autoLoginInProgress.value = false
                        navigateToMain()
                    }
                }

                override fun onError(error: String?, isNetworkError: Boolean) {
                    if (isNetworkError) {
                        // Solo permitir modo offline si ya hubo al menos un login + sync previo
                        if (hasPreviousSuccessfulLogin()) {
                            Log.w("LoginActivity", "Error de red, usando modo offline. Error: $error")
                            show("Sin conexión. Entrando en modo offline.", NotificationType.WARNING)
                            navigateToMain(isOffline = true)
                        } else {
                            Log.w("LoginActivity", "Error de red sin sesión previa. Permaneciendo en login. Error: $error")
                            runOnUiThread {
                                show("No hay conexión al servidor y no existe una sesión previa para modo offline.", NotificationType.ERROR)
                            }
                        }
                    } else {
                        Log.e("LoginActivity", "Login error: $error")
                        prefs.edit()
                            .remove("saved_username")
                            .remove("saved_password")
                            .apply()
                        runOnUiThread {
                            show("$error", NotificationType.ERROR)
                        }
                    }
                    autoLoginInProgress.value = false
                }
            }
        )
    }
}