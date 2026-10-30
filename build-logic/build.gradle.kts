plugins {
    `kotlin-dsl`
}

// compileOnly: the root build.gradle.kts puts the real plugins on the classpath,
// so versions come from one place, gradle/libs.versions.toml.
dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "coachpoulpy.kmp-library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpWebLibrary") {
            id = "coachpoulpy.kmp-web-library"
            implementationClass = "KmpWebLibraryConventionPlugin"
        }
        register("i18n") {
            id = "coachpoulpy.i18n"
            implementationClass = "I18nConventionPlugin"
        }
    }
}
