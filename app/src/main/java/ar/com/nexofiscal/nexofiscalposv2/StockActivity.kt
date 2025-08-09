package ar.com.nexofiscal.nexofiscalposv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.com.nexofiscal.nexofiscalposv2.db.viewmodel.StockViewModel
import ar.com.nexofiscal.nexofiscalposv2.screens.StockScreen
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.NexoFiscalPOSV2Theme

class StockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NexoFiscalPOSV2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: StockViewModel = viewModel()
                    StockScreen(viewModel = viewModel)
                }
            }
        }
    }
}
