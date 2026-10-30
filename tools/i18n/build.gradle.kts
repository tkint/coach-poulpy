// Turns the translation files in i18n/ into Android and iOS string files, the shared `domain` text
// and the merged exercise catalog (ADR 0014, ADR 0015). The coachpoulpy.i18n plugin in build-logic runs it.
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
