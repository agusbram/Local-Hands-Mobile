import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    //For plugin KSP for Room
    alias(libs.plugins.ksp)

    id("dagger.hilt.android.plugin")
}

android {
    // Configuracion de Variables de Entorno
    defaultConfig {
        // Leer desde local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        buildConfigField("String", "EMAIL_USER", "\"${localProperties.getProperty("EMAIL_USER", "")}\"")
        buildConfigField("String", "EMAIL_PASS", "\"${localProperties.getProperty("EMAIL_PASS", "")}\"")
    }


    namespace = "com.undef.localhandsbrambillafunes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.undef.localhandsbrambillafunes"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_17 // Actualiza a Java 17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true // Habilitar para usar variables de entorno
        compose = true
    }

    // Paquetes que deben ser ignorados
    packaging {
        resources {
            excludes += setOf(
                "META-INF/NOTICE.md",
                "META-INF/LICENSE.md",
                "META-INF/NOTICE",
                "META-INF/LICENSE",
                "META-INF/NOTICE.txt",
                "META-INF/LICENSE.txt",
                "META-INF/DEPENDENCIES"
            )
        }
    }
}

dependencies {
    val roomVersion = "2.7.2"
    val hiltVersion = "2.51.1" // USAR VERSIÓN MÁS RECIENTE

    //For Room library for database
    implementation("androidx.room:room-runtime:$roomVersion")
    //Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")
    //Plugin for Room
    ksp("androidx.room:room-compiler:$roomVersion")
    //Paging 3 Integration
    implementation("androidx.room:room-paging:$roomVersion")

    // Depencia HILT para Compose
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0")

    // Dependencias para enviar correos de verificacion
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    // Dependencia BCrypt para hashear contraseñas
    implementation("at.favre.lib:bcrypt:0.10.2")

    // Dependencia de Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Converter de Gson para Retrofit
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)

    // Agregar Dependencia de navegacion de Jetpack Compose
    implementation(libs.androidx.navigation.compose)
    //Dependencia para utilizar Visibilities en login screen
    implementation(libs.compose.icons.extended)
    //Dependencia para utilizar rememberAsyncImagePainter
    implementation(libs.coil.compose)

    implementation(libs.androidx.ui.tooling.preview) // Para @Preview
    debugImplementation(libs.androidx.ui.tooling)    // Para ver Previews en Android Studio

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}