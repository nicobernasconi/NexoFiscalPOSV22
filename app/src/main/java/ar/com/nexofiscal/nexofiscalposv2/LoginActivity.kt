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

class LoginActivity : ComponentActivity() {

    private val BASE_URL = "https://test.nexofiscaltest.com.ar/"
    private lateinit var prefs: SharedPreferences
    private val SYNC_INTERVAL_HOURS = 24L

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
                        realizarLogin(usuario, password) {
                            showSyncScreen = true
                        }
                    }
                }
                NotificationHost()
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
            realizarLogin(savedUser, savedPass) {
                setContent {
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
                        navigateToMain()
                    }
                }

                override fun onError(error: String?, isNetworkError: Boolean) {
                    if (isNetworkError) {
                        Log.w("LoginActivity", "Error de red, entrando en modo offline. Error: $error")
                        navigateToMain(isOffline = true)
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
                }
            }
        )
    }
}