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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // Actualizado para Kotlin 2.0.21 + BOM 2024.09.00
        kotlinCompilerExtensionVersion = "1.7.0"
    }
}

dependencies {
    // BOM centraliza versiones Compose
    implementation(platform(libs.androidx.compose.bom))

    // Core Compose (sin versiones explícitas)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.material3) // material3 del BOM
    implementation("androidx.compose.material:material-icons-extended")

    // Text (opcional si requerido específicamente)
    implementation("androidx.compose.ui:ui-text")

    // Activity Compose (usa versión del catálogo, ya alineada a 1.10.1)
    implementation(libs.androidx.activity.compose)

    // Kotlin reflect alineado a la versión del plugin
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.21")

    // Android Core
    implementation(libs.androidx.core.ktx)

    // Navegación Compose
    implementation(libs.androidx.navigation.compose)

    // Lifecycle / ViewModel Compose
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Paging (alineado a Compose 1.7, evitar alpha antigua que rompe anotaciones)
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.3.2")
    implementation("androidx.room:room-paging:2.6.1")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // Gson (una sola vez)
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Local broadcast
    implementation(libs.androidx.localbroadcastmanager)

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Cámara / ML Kit
    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.0")

    // Libs locales
    implementation(files("libs/core-3.2.1.jar"))
    implementation(files("libs/emv_2.0.0_R240607.jar"))
    implementation(files("libs/SmartPos_1.9.4_R250117.jar"))

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
