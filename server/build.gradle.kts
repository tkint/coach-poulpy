plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("ch.coachpoulpy.server.MainKt")
}

// The server's own Postgres schema, separate from the phone's SQLite one (ADR 0022). Each
// migration is a numbered .sqm file in src/main/sqldelight/migrations; the schema is derived
// from them, and Database.migrate applies the missing ones on start.
sqldelight {
    databases {
        create("ServerDatabase") {
            packageName.set("ch.coachpoulpy.server.db")
            dialect(libs.sqldelight.postgresql.dialect)
            srcDirs("src/main/sqldelight")
            deriveSchemaFromMigrations.set(true)
        }
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.catalog)
    implementation(platform(libs.http4k.bom))
    implementation(libs.http4k.core)
    implementation(libs.http4k.api.openapi)
    implementation(libs.http4k.format.kotlinx.serialization)
    implementation(libs.sqldelight.jdbc.driver)
    implementation(libs.hikaricp)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.slf4j.simple)
    testImplementation(kotlin("test"))
    testImplementation(libs.embedded.postgres)
    testImplementation(platform(libs.embedded.postgres.binaries.bom))
}
