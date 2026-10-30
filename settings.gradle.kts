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
    // The Kotlin/JS plugin registers its own Node.js and Yarn download repositories on the
    // root project; preferring these settings ignores them, so they are declared here.
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        ivy("https://nodejs.org/dist") {
            name = "Node.js distributions"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
        ivy("https://github.com/yarnpkg/yarn/releases/download") {
            name = "Yarn distributions"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
    }
}

include(
    ":core:model",
    ":core:engine",
    ":core:stats",
    ":types:treadmill",
    ":catalog",
    ":data:local",
    ":device:ftms",
    ":export:fit",
    ":shared",
    ":app-android",
    ":server",
    ":tools:i18n",
)
