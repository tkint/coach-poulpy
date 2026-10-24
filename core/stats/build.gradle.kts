plugins {
    id("coachpoulpy.kmp-library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.engine)
        }
    }
}
