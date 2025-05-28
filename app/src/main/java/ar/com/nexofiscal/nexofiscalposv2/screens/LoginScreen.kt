package ar.com.nexofiscal.nexofiscalposv2.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ar.com.nexofiscal.nexofiscalposv2.R

private val AzulNexo = Color(0xFF00AEEF)
private val Blanco   = Color(0xFFFFFFFF)
private val BordeSuave = RoundedCornerShape(4.dp)

/**
 * Pantalla de login.
 *
 * @param onLogin Callback que se dispara con usuario y contraseña cuando
 *                el usuario toca INGRESAR y ambos campos no están vacíos.
 */
@Composable
fun LoginScreen(
    onLogin: (usuario: String, password: String) -> Unit
) {
    var usuario      by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var usuarioError by remember { mutableStateOf<String?>(null) }
    var passError    by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blanco)
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulNexo)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.header_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(width = 175.dp, height = 36.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Inicio de Sesión",
                style = MaterialTheme.typography.titleMedium,
                color = Blanco
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Formulario
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Usuario
            OutlinedTextField(
                value = usuario,
                onValueChange = {
                    usuario = it
                    usuarioError = null
                },
                label = { Text("Usuario") },
                isError = usuarioError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = BordeSuave
            )
            usuarioError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passError = null
                },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                isError = passError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = BordeSuave
            )
            passError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón INGRESAR
            Button(
                onClick = {
                    var ok = true
                    if (usuario.isBlank()) {
                        usuarioError = "Ingrese el usuario"
                        ok = false
                    }
                    if (password.isBlank()) {
                        passError = "Ingrese la contraseña"
                        ok = false
                    }
                    if (ok) {
                        onLogin(usuario.trim(), password.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzulNexo),
                shape = BordeSuave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "INGRESAR", color = Blanco)
            }
        }
    }
}
