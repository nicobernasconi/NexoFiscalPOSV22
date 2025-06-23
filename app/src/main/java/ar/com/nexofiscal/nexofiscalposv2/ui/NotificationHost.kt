package ar.com.nexofiscal.nexofiscalposv2.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import ar.com.nexofiscal.nexofiscalposv2.R

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotificationHost() {
    // Estado de la notificación visible
    var current by remember { mutableStateOf<NotificationData?>(null) }

    // Coleccionamos eventos
    LaunchedEffect(Unit) {
        NotificationManager.events.collectLatest { data ->
            current = data
            // dura 3 segundos
            delay(3000)
            current = null
        }
    }

    // Animación de entrada/salida desde arriba
    AnimatedVisibility(
        visible = current != null,
        enter = slideInVertically { -it / 2 } + fadeIn(),
        exit  = slideOutVertically { -it / 2 } + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .wrapContentHeight()

    ) {
        current?.let { data ->
            val (bgColor, iconRes) = when (data.type) {
                NotificationType.SUCCESS -> {
                    Pair(
                        colorResource(id = R.color.noti_exito),
                        R.drawable.ic_check
                    )
                }
                NotificationType.ERROR   -> {
                    Pair(
                        colorResource(id = R.color.noti_error),
                        R.drawable.ic_close
                    )
                }
                NotificationType.INFO    -> {
                    Pair(
                        colorResource(id = R.color.noti_info),
                        R.drawable.ic_info
                    )
                }
                NotificationType.WARNING -> {
                    Pair(
                        colorResource(id = R.color.noti_warning),
                        R.drawable.ic_warning
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(bgColor, RoundedCornerShape(5.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = data.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 3
                )
            }
        }
    }
}
