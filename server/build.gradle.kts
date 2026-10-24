plugins {
    alias(libs.plugins.kotlin.jvm)
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
    implementation(platform(libs.http4k.bom))
    implementation(libs.http4k.core)
    runtimeOnly(libs.slf4j.simple)
    testImplementation(kotlin("test"))
}
