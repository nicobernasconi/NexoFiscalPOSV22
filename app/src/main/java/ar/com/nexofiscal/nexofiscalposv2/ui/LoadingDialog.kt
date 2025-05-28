// LoadingDialog.kt
package ar.com.nexofiscal.nexofiscalposv2.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ar.com.nexofiscal.nexofiscalposv2.R

/**
 * Muestra un diálogo modal de carga con logo, spinner y mensaje.
 *
 * @param show    controla si se debe mostrar el diálogo.
 * @param message texto a mostrar debajo del spinner. Por defecto "Cargando…".
 */
@Composable
fun LoadingDialog(
    show: Boolean,
    message: String = "Cargando…"
) {
    if (!show) return

    Dialog(onDismissRequest = { /* no se cierra tocando fuera */ }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .background(Color.LightGray)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.header_logo),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(120.dp)
                        .height(60.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Spinner
                CircularProgressIndicator(
                    color = Color(0xFF00AEEF),
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mensaje
                Text(
                    text = message,
                    color = Color.Black
                )
            }
        }
    }
}
