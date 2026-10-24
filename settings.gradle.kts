rootProject.name = "coach-poulpy"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(
    ":core:model",
    ":core:engine",
    ":core:stats",
    ":types:strength",
    ":types:conditioning",
    ":types:rehab",
    ":types:mobility",
    ":catalog",
    ":data:local",
    ":export:fit",
    ":shared",
    ":app-android",
    ":server",
)
