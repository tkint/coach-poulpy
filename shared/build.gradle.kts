plugins {
    id("coachpoulpy.kmp-library")
    id("coachpoulpy.i18n")
    alias(libs.plugins.kotlin.serialization)
}

// From the translation files (ADR 0014): the domain words Labels and PoulpyCoach read, and the iOS app's
// string tables, made before Xcode embeds this framework (app-ios/project.yml points at them).
i18n.kotlinText("domain", "ch.coachpoulpy.shared", "DomainText")
i18n.appleStrings("app")

kotlin {
    // The framework Swift imports as `import Shared`.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(projects.core.model)
            // Device kinds show in Settings › Devices, so Swift sees them by their own name.
            export(projects.device.ftms)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.core.engine)
            api(projects.catalog)
            api(projects.data.local)
            api(projects.device.ftms)
            api(projects.types.treadmill)
            implementation(libs.kotlinx.coroutines.core)
        }
        jvmTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
