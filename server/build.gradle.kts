plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("ch.coachpoulpy.server.MainKt")
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.catalog)
    implementation(platform(libs.http4k.bom))
    implementation(libs.http4k.core)
    implementation(libs.http4k.api.openapi)
    implementation(libs.http4k.format.kotlinx.serialization)
    runtimeOnly(libs.slf4j.simple)
    testImplementation(kotlin("test"))
}
