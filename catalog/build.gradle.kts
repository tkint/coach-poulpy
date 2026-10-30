plugins {
    id("coachpoulpy.kmp-library")
    id("coachpoulpy.i18n")
    alias(libs.plugins.kotlin.serialization)
}

// The catalog's structure is edited as JSON in catalog/data and its text in i18n/messages/*/catalog.json
// (ADR 0015). The build merges them and embeds the result into the code, so every platform reads the
// same document with no file access at runtime.
val mergeCatalogText = i18n.catalog(layout.projectDirectory.file("data/exercises.json"))

val generateCatalogSource by tasks.registering {
    val input = mergeCatalogText.flatMap { it.outputDirectory.file("exercises.json") }
    val output = layout.buildDirectory.dir("generated/catalog")
    inputs.file(input)
    outputs.dir(output)
    doLast {
        val json = input.get().asFile.readText()
        val file = output.get().file("ch/coachpoulpy/catalog/BundledCatalogJson.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            "package ch.coachpoulpy.catalog\n\n" +
                "// Generated from catalog/data/exercises.json and i18n/messages/*/catalog.json. Do not edit.\n" +
                "internal const val BUNDLED_EXERCISES_JSON: String = \"\"\"" +
                json.replace("$", "\${'$'}") +
                "\"\"\"\n",
        )
    }
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generateCatalogSource)
            dependencies {
                api(projects.core.model)
            }
        }
    }
}
