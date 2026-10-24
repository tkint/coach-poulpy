plugins {
    id("coachpoulpy.kmp-web-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.engine)
        }
    }
}
