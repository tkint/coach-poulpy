import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("coachpoulpy.i18n")
}

kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

android {
    namespace = "ch.coachpoulpy.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ch.coachpoulpy"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // CI numbers each build by the minute it ran, so the APK built last always installs over the one
        // on the phone, whichever branch it came from. Re-running an older run gives it a fresh number.
        versionCode = providers.environmentVariable("POULPY_VERSION_CODE").orNull?.toInt() ?: 1
        versionName = "0.1.0"
    }

    // One fixed key for every build, so updates install over each other instead of asking to uninstall.
    // CI passes it through environment variables (see the README); on a dev computer, keystore.properties
    // next to this file does the same. Without either, Android's usual per-computer debug key is used.
    val signing = signingKey()
    if (signing != null) {
        signingConfigs.getByName("debug") {
            storeFile = signing.storeFile
            storePassword = signing.storePassword
            keyAlias = signing.keyAlias
            keyPassword = signing.keyPassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Sideloaded on family phones, so release builds share the same key as debug builds.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// values*/strings.xml are generated from i18n/messages/*/app.json (ADR 0014).
i18n.androidStrings("app")

dependencies {
    implementation(projects.shared)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

class SigningKey(val storeFile: File, val storePassword: String, val keyAlias: String, val keyPassword: String)

fun signingKey(): SigningKey? {
    val properties = Properties()
    val local = file("keystore.properties")
    if (local.exists()) local.inputStream().use { properties.load(it) }
    fun value(property: String, env: String) = properties.getProperty(property) ?: providers.environmentVariable(env).orNull
    val store = value("storeFile", "POULPY_KEYSTORE_FILE")?.takeIf { it.isNotBlank() } ?: return null
    val storePassword = value("storePassword", "POULPY_KEYSTORE_PASSWORD") ?: error("The signing key has no store password")
    return SigningKey(
        storeFile = file(store),
        storePassword = storePassword,
        keyAlias = value("keyAlias", "POULPY_KEY_ALIAS") ?: "poulpy",
        keyPassword = value("keyPassword", "POULPY_KEY_PASSWORD") ?: storePassword,
    )
}
