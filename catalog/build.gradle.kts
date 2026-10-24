plugins {
    id("coachpoulpy.kmp-library")
    alias(libs.plugins.kotlin.serialization)
}

// The catalog is edited as JSON in catalog/data and embedded into the code at build
// time, so every platform reads it the same way with no file access at runtime.
val generateCatalogSource by tasks.registering {
    val input = layout.projectDirectory.file("data/exercises.json")
    val output = layout.buildDirectory.dir("generated/catalog")
    inputs.file(input)
    outputs.dir(output)
    doLast {
        val json = input.asFile.readText()
        val file = output.get().file("ch/coachpoulpy/catalog/BundledCatalogJson.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            "package ch.coachpoulpy.catalog\n\n" +
                "// Generated from catalog/data/exercises.json. Do not edit.\n" +
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
