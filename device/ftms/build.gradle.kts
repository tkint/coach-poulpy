plugins {
    id("coachpoulpy.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(libs.kotlinx.coroutines.core)
            implementation(libs.kable.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
