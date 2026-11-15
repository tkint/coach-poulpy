plugins {
    id("coachpoulpy.kmp-library")
}

// Test data only: only test source sets depend on this module (ADR 0032). The catalog's own tests live here
// because they run on the sample, and the catalog module can't depend on a module that depends on it.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.catalog)
        }
    }
}
