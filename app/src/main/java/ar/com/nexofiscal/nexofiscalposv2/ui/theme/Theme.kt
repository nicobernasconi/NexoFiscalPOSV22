package ar.com.nexofiscal.nexofiscalposv2.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AzulNexo,
    secondary = TextoGrisOscuro,
    tertiary = CC
)

// --- MODIFICACIÓN 1: Se definen los colores de texto por defecto ---
private val LightColorScheme = lightColorScheme(
    primary = AzulNexo,
    secondary = TextoGrisOscuro,
    tertiary = CC,

    // Se establece el color del texto por defecto a negro para las superficies y fondos.
    onSurface = NegroNexo,
    onBackground = NegroNexo,
    onPrimary = Blanco // Se asegura que el texto sobre botones de color primario sea blanco.
)

@Composable
fun NexoFiscalPOSV2Theme(
    // Parámetros ya no son necesarios porque forzaremos un único tema.
    content: @Composable () -> Unit
) {

    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}