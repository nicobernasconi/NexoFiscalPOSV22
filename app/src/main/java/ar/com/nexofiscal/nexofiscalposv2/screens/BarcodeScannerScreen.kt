package ar.com.nexofiscal.nexofiscalposv2.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.compose.LocalLifecycleOwner
import ar.com.nexofiscal.nexofiscalposv2.R
import ar.com.nexofiscal.nexofiscalposv2.ui.theme.BordeSuave
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.zcs.sdk.DriverManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

private enum class ScannerMode { ZCS, CAMERA }

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onDismiss: () -> Unit,
    onCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val prefs = remember { context.getSharedPreferences("nexofiscal", Context.MODE_PRIVATE) }

    // Sin gestión de KeyEvent: el SDK/IME inyecta directamente en el TextField; Enter (\n/\r) se detecta en onValueChange/onDone.

    // --- Detección de Hardware ---
    val hqScanner = remember { try { DriverManager.getInstance().hQrsannerDriver
    } catch (_: Exception) { null } }
    val hasZcsScanner = remember { hqScanner != null }
    val hasDeviceCamera = remember { packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) }

    // --- Lógica de modo inicial con persistencia ---
    val initialMode = remember {
        val savedModeName = prefs.getString("scanner_last_mode", null)
        val savedMode = savedModeName?.let { try { ScannerMode.valueOf(it) } catch (_: IllegalArgumentException) { null } }

        // Prioridad 1: Usar el modo guardado si el hardware está disponible.
        if (savedMode == ScannerMode.ZCS && hasZcsScanner) {
            ScannerMode.ZCS
        } else if (savedMode == ScannerMode.CAMERA && hasDeviceCamera) {
            ScannerMode.CAMERA
        } else {
            // Prioridad 2: Fallback a la detección por defecto si no hay modo guardado o no es válido.
            if (hasZcsScanner) ScannerMode.ZCS else ScannerMode.CAMERA
        }
    }

    var currentMode by remember { mutableStateOf(initialMode) }

    // Nota: ya no deshabilitamos el lector en modo Cámara; Enter siempre se captura en esta pantalla

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear Código") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Mostrar botón para cambiar de modo solo si ambas opciones existen
                    if (hasZcsScanner && hasDeviceCamera) {
                        IconButton(onClick = {
                            // Cambiar el estado
                            val newMode = if (currentMode == ScannerMode.ZCS) ScannerMode.CAMERA else ScannerMode.ZCS
                            currentMode = newMode

                            // Guardar la nueva preferencia
                            prefs.edit { putString("scanner_last_mode", newMode.name) }
                        }) {
                            Icon(
                                painter = if (currentMode == ScannerMode.ZCS) {
                                    painterResource(id = R.drawable.ic_camera)
                                } else {
                                    painterResource(id = R.drawable.ic_barcode)
                                },
                                contentDescription = "Cambiar modo de escáner"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // Contenido de la pantalla según el modo seleccionado
        when (currentMode) {
            ScannerMode.ZCS -> {
                ZcsScannerView(
                    modifier = Modifier.padding(paddingValues),
                    hqScanner = hqScanner,
                    onCodeScanned = onCodeScanned
                )
            }
            ScannerMode.CAMERA -> {
                CameraScannerView(
                    modifier = Modifier.padding(paddingValues),
                    onCodeScanned = onCodeScanned
                )
            }
        }
    }
}




@Composable
private fun ZcsScannerView(
    modifier: Modifier = Modifier,
    hqScanner: com.zcs.sdk.HQrsanner?,
    onCodeScanned: (String) -> Unit
) {
    var manualCode by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Gestiona el ciclo de vida del hardware ZCS
    DisposableEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            hqScanner?.QRScanerPowerCtrl(1.toByte())
        }
        onDispose {
            CoroutineScope(Dispatchers.IO).launch {
                hqScanner?.QRScanerPowerCtrl(0.toByte())
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_barcode),
            contentDescription = "Icono de código de barras",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Modo Escáner Hardware (ZCS)", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Presione el botón físico del dispositivo o el botón de abajo para activar el láser",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = manualCode,
            onValueChange = { newText ->
                // --- LÍNEA DE DIAGNÓSTICO AÑADIDA ---
                Log.d("BarcodeScannerDebug", "Input recibido: '$newText' (Largo: ${newText.length})")
                // ------------------------------------

                manualCode = newText
                // Se verifica si el texto contiene cualquier carácter de nueva línea (\n) o retorno de carro (\r).
                if (newText.contains("\n") || newText.contains("\r")) {
                    // Limpiamos el código de estos caracteres especiales y espacios.
                    val cleanCode = newText.trim().replace("\n", "").replace("\r", "")
                    if (cleanCode.isNotEmpty()) {
                        focusManager.clearFocus()
                        onCodeScanned(cleanCode)
                    }
                }
            },
            label = { Text("Código de Producto") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if(manualCode.isNotBlank()) onCodeScanned(manualCode.trim())
            })
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    hqScanner?.QRScanerCtrl(1.toByte())
                    SystemClock.sleep(10)
                    hqScanner?.QRScanerCtrl(0.toByte())
                }
                focusRequester.requestFocus()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = BordeSuave
        ) {
            Text("ACTIVAR LÁSER MANUALMENTE")
        }
    }
}

@ExperimentalGetImage
@Composable
private fun CameraScannerView(
    modifier: Modifier = Modifier,
    onCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCamPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCamPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCamPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (hasCamPermission) {
            var codeFound by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalyzer = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                        val image = imageProxy.image
                                        if (image != null && !codeFound) {
                                            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

                                            val options = BarcodeScannerOptions.Builder()
                                                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                                                .build()
                                            val scanner = BarcodeScanning.getClient(options)

                                            scanner.process(inputImage)
                                                .addOnSuccessListener { barcodes ->
                                                    if (codeFound) return@addOnSuccessListener
                                                    var shouldStop = false
                                                    for (barcode in barcodes) {
                                                        if (shouldStop) break
                                                        barcode.rawValue?.let { code ->
                                                            codeFound = true
                                                            onCodeScanned(code)
                                                            shouldStop = true
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("CameraScannerView", "Error en ML Kit", e)
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
                            } catch(exc: Exception) {
                                Log.e("CameraScannerView", "Fallo al vincular casos de uso de la cámara", exc)
                            }
                        }, ContextCompat.getMainExecutor(context))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Superposición visual (Viewfinder)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1.5f)
                        .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Se necesita permiso de la cámara.", textAlign = TextAlign.Center)
            }
        }
    }
}