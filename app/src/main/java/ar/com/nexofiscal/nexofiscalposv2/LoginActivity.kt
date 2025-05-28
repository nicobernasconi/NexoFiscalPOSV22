package ar.com.nexofiscal.nexofiscalposv2

                import android.app.NotificationManager
                import android.content.Context
                import android.content.Intent
                import android.hardware.display.DisplayManager
                import android.os.Bundle
                import android.util.Log
                import androidx.activity.ComponentActivity
                import androidx.activity.compose.setContent
                import androidx.compose.runtime.collectAsState
                import androidx.compose.runtime.getValue
                import ar.com.nexofiscal.nexofiscalposv2.network.LoginHelper
                import ar.com.nexofiscal.nexofiscalposv2.screens.LoginScreen
                import ar.com.nexofiscal.nexofiscalposv2.ui.LoadingDialog
                import ar.com.nexofiscal.nexofiscalposv2.ui.LoadingManager
                import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationHost
                import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationType
                import ar.com.nexofiscal.nexofiscalposv2.ui.theme.NexoFiscalPOSV2Theme
                import org.json.JSONObject
                import ar.com.nexofiscal.nexofiscalposv2.ui.NotificationManager.show

                class LoginActivity : ComponentActivity() {

                    private val BASE_URL = "https://test.nexofiscaltest.com.ar/"

                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)

                        setContent {
                            NexoFiscalPOSV2Theme {
                                val isLoading by LoadingManager.isLoading.collectAsState()
                                LoginScreen { usuario, password ->

                                    realizarLogin(usuario, password)
                                }
                                NotificationHost()
                                LoadingDialog(
                                    show    = isLoading,
                                    message = "Ingresando..."
                                )

                            }
                        }
                    }

                    private fun realizarLogin(usuario: String, password: String) {
                        LoadingManager.show()
                        Thread {
                            LoginHelper.login(
                                this,
                                BASE_URL,
                                usuario,
                                password,
                                object : LoginHelper.LoginCallback {
                                    override fun onSuccess(response: JSONObject?) {
                                        Log.d("LoginActivity", "Login successful: $response")
                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        LoadingManager.hide()
                                        finish()
                                    }

                                    override fun onError(error: String?) {
                                        Log.e("LoginActivity", "Login error: $error")
                                        show("$error", NotificationType.ERROR)
                                        LoadingManager.hide()
                                    }
                                }
                            )
                        }.start()
                    }
                }