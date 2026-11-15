plugins {
    id("coachpoulpy.kmp-library")
    alias(libs.plugins.kotlin.serialization)
}

// The catalog's exercises live in the server database and reach phones by download (ADR 0033);
// this module holds only their shape and the rules about them.
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.model)
            }
        }
    }
}
