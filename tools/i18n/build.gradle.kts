// Turns the translation files in i18n/ into Android and iOS string files and the shared `domain` text
// (ADR 0014). The coachpoulpy.i18n plugin in build-logic runs it.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
}
