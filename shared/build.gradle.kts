plugins {
    id("coachpoulpy.kmp-library")
}

kotlin {
    // The framework Swift imports as `import Shared`.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(projects.core.model)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.core.engine)
            api(projects.catalog)
            api(projects.data.local)
            implementation(projects.types.strength)
            implementation(projects.types.conditioning)
            implementation(projects.types.rehab)
            implementation(projects.types.mobility)
            implementation(libs.kotlinx.coroutines.core)
        }
        jvmTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
