plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "ar.com.nexofiscal.nexofiscalposv2"
    compileSdk = 35

    defaultConfig {
        applicationId = "ar.com.nexofiscal.nexofiscalposv2"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1" // asegurate que coincida con tu BOM
    }
}

dependencies {

 // --- Dependencias de Compose UI ---
     implementation("androidx.compose.ui:ui-text")        // text-input, que incluye KeyboardOptions
     implementation("androidx.compose.foundation:foundation") // Fundaciones de UI
     implementation("androidx.compose.ui:ui:1.5.0") // Núcleo de UI Compose
     implementation("androidx.compose.material3:material3:1.1.0") // Material Design 3
     implementation(libs.androidx.core.ktx) // Extensiones de Kotlin para Android

     implementation(libs.androidx.activity.compose) // Integración de actividades con Compose
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation(platform(libs.androidx.compose.bom)) // BOM para Compose
     implementation(libs.androidx.ui) // Componentes de UI
     implementation(libs.androidx.ui.graphics) // Gráficos de UI
     implementation(libs.androidx.ui.tooling.preview) // Herramientas de previsualización
     implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0") // Reflexión en Kotlin
     implementation("com.google.code.gson:gson:2.10.1") // Librería Gson para JSON
    implementation("androidx.compose.material:material-icons-extended:1.5.1")
    implementation("com.google.code.gson:gson:2.10.1")
     // --- Jetpack ViewModel & LiveData ---


     // --- Jetpack Navigation para Compose ---
     implementation(libs.androidx.navigation.compose) // Navegación en Compose

     // --- Retrofit (Cliente HTTP) ---
     implementation("com.squareup.retrofit2:retrofit:2.9.0") // Core de Retrofit
     implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Conversor JSON con Gson
     implementation("com.squareup.okhttp3:logging-interceptor:4.9.3") // Interceptor para logs de red
     implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    //PAGINACION
    implementation ("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation ("androidx.paging:paging-compose:3.3.0-alpha02")
    implementation ("androidx.room:room-paging:2.6.1")
    implementation(libs.androidx.localbroadcastmanager)


    // --- Room (Base de datos local) ---
    val roomVersion = "2.6.1" // Reemplaza con tu versión deseada
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

     implementation(files("libs/core-3.2.1.jar")) // Dependencia local
     implementation(files("libs/emv_2.0.0_R240607.jar")) // Dependencia local
     implementation(files("libs/SmartPos_1.9.4_R250117.jar")) // Dependencia local

    implementation ("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")


     // --- Dependencias de Testing ---
     testImplementation(libs.junit) // JUnit para pruebas unitarias
     androidTestImplementation(libs.androidx.junit) // JUnit para pruebas instrumentadas
     androidTestImplementation(libs.androidx.espresso.core) // Espresso para pruebas de UI
     androidTestImplementation(platform(libs.androidx.compose.bom)) // BOM para pruebas de Compose
     androidTestImplementation(libs.androidx.ui.test.junit4) // Pruebas de UI con JUnit4
     debugImplementation(libs.androidx.ui.tooling) // Herramientas de depuración para Compose
     debugImplementation(libs.androidx.ui.test.manifest) // Manifest para pruebas de UI


    implementation ("androidx.camera:camera-core:1.4.2")
    implementation ("androidx.camera:camera-camera2:1.4.2")
    implementation ("androidx.camera:camera-lifecycle:1.4.2")
    implementation ("androidx.camera:camera-view:1.4.2")

    // Para ML Kit de Google: Barcode Scanning
    implementation ("com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
