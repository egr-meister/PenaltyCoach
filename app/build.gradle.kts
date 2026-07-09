import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

// ---------------------------------------------------------------------------
// Read local.properties (never committed) for API config. Fall back safely
// to placeholders so the project always builds, even with no token present.
// ---------------------------------------------------------------------------
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) {
        FileInputStream(f).use { load(it) }
    }
}

fun readConfig(key: String, default: String): String {
    // Priority: environment variable (CI) -> local.properties -> default.
    return (System.getenv(key) ?: localProps.getProperty(key) ?: default).trim()
}

val footballApiBaseUrl: String = readConfig("FOOTBALL_API_BASE_URL", "https://api.football-data.org/v4")
val footballApiToken: String = readConfig("FOOTBALL_DATA_API_TOKEN", "your_api_token_here")

// ---------------------------------------------------------------------------
// Release signing config. Values come ONLY from environment variables
// (GitHub Secrets in CI). Nothing sensitive is ever hardcoded here.
// ---------------------------------------------------------------------------
val keystorePath: String? = System.getenv("ANDROID_KEYSTORE_PATH")
val keystorePassword: String? = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val keyAlias: String? = System.getenv("ANDROID_KEY_ALIAS")
val keyPasswordEnv: String? = System.getenv("ANDROID_KEY_PASSWORD")
val hasReleaseSigning = !keystorePath.isNullOrBlank() &&
    !keystorePassword.isNullOrBlank() &&
    !keyAlias.isNullOrBlank() &&
    !keyPasswordEnv.isNullOrBlank()

android {
    namespace = "com.penaltycoach.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.penaltycoach.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Expose API configuration through BuildConfig fields (no hardcoding in source).
        buildConfigField("String", "FOOTBALL_API_BASE_URL", "\"$footballApiBaseUrl\"")
        buildConfigField("String", "FOOTBALL_DATA_API_TOKEN", "\"$footballApiToken\"")
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(keystorePath!!)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                keyPassword = keyPasswordEnv
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        getByName("release") {
            // R8 / resource shrinking enabled for release. The keep rules in
            // proguard-rules.pro protect kotlinx.serialization + Retrofit models.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use the real release keystore when signing material is provided,
            // otherwise leave unsigned so local debug builds never fail.
            signingConfig = if (hasReleaseSigning) signingConfigs.getByName("release") else null
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
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Keep CI release builds resilient: lint should never block artifact output.
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.retrofit)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    debugImplementation(libs.okhttp.logging.interceptor)
}
