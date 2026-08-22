import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.baselineprofile)
}

private val propriedadesLocais: Properties = Properties().apply {
    val arquivo = rootProject.file("local.properties")
    if (arquivo.exists()) arquivo.inputStream().use { load(it) }
}

fun propriedadeLocal(nome: String, padrao: String = ""): String =
    propriedadesLocais.getProperty(nome)?.trim()?.takeIf { it.isNotBlank() } ?: padrao

val mapsApiKey = propriedadeLocal("MAPS_API_KEY")
val baseUrlDebug = propriedadeLocal("BASE_URL_DEBUG", "http://192.168.15.57:8090/api/")
val baseUrlRelease = propriedadeLocal("BASE_URL_RELEASE")

val compilandoRelease = gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }
if (compilandoRelease) {
    require(baseUrlRelease.isNotBlank()) {
        "Defina BASE_URL_RELEASE em local.properties (ex.: https://api.seudominio.com/api/)."
    }
    require(mapsApiKey.isNotBlank()) { "Defina MAPS_API_KEY em local.properties." }
} else if (mapsApiKey.isBlank()) {
    logger.warn("MAPS_API_KEY ausente em local.properties: o mapa não vai carregar.")
}

android {
    namespace = "com.winyc.elo"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.winyc.elo"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"$baseUrlDebug\"")
            // O backend de desenvolvimento roda em HTTP na rede local.
            manifestPlaceholders["cleartextTraffic"] = true
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"$baseUrlRelease\"")
            manifestPlaceholders["cleartextTraffic"] = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildTypes.configureEach {
        if (name.startsWith("nonMinified") || name.startsWith("benchmark")) {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

baselineProfile {
    mergeIntoMain = true
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.tink.android)
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.installations)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}