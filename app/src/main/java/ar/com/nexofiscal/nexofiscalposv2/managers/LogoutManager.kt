package ar.com.nexofiscal.nexofiscalposv2.managers

import android.content.Context
import android.content.Intent
import ar.com.nexofiscal.nexofiscalposv2.LoginActivity
import ar.com.nexofiscal.nexofiscalposv2.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Gestor centralizado para el proceso de cierre de sesión.
 */
object LogoutManager {


    fun logout(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            // Borrar todas las preferencias guardadas (token, usuario, etc.)
            val prefs = context.getSharedPreferences("nexofiscal", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            // Borrar todos los datos de la base de datos local
            val database = AppDatabase.getInstance(context.applicationContext)
            database.clearAllTables()

            // Volver a la pantalla de Login en el hilo principal
            withContext(Dispatchers.Main) {
                val intent = Intent(context, LoginActivity::class.java).apply {
                    // Estas flags aseguran que el usuario no pueda "volver" a la pantalla principal
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            }
        }
    }
}