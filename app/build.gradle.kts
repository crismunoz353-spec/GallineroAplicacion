plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Agregamos el plugin de Google Services aquí también:
    id("com.google.gms.google-services")
}

android {
    namespace = "com.inacap.gallineroiot"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.inacap.gallineroiot"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Se eliminó 'kotlinOptions' de aquí porque causaba el error

    buildFeatures {
        compose = true
    }
}

// Nueva configuración de Kotlin (reemplaza a kotlinOptions)
kotlin {
    jvmToolchain(8)
}

dependencies {
    // Dependencias por defecto de Android Studio (NO BORRAR)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --- DEPENDENCIAS DE FIREBASE ---
    // Firebase BoM (Maneja las versiones automáticamente)
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))

    // Autenticación (Login y Registro)
    implementation("com.google.firebase:firebase-auth")

    // Base de datos en tiempo real (Firestore)
    implementation("com.google.firebase:firebase-firestore")
}