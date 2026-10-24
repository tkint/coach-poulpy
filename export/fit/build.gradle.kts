plugins {
    id("coachpoulpy.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
        }
    }
}
