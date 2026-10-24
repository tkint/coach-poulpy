import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention for every shared Kotlin Multiplatform module: Android, iOS and JVM targets.
 * The JVM target lets the server reuse the domain and gives fast tests on any machine.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.library")

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun version(name: String) = libs.findVersion(name).get().requiredVersion.toInt()

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(21)

            androidTarget {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
            }
            jvm()
            iosArm64()
            iosSimulatorArm64()

            compilerOptions {
                optIn.add("kotlin.time.ExperimentalTime")
                optIn.add("kotlin.uuid.ExperimentalUuidApi")
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }

            sourceSets.getByName("commonTest").dependencies {
                implementation(kotlin("test"))
            }
        }

        extensions.configure<LibraryExtension> {
            // Module ":core:model" becomes namespace "ch.coachpoulpy.core.model".
            namespace = "ch.coachpoulpy" + path.replace(':', '.').replace('-', '.')
            compileSdk = version("android-compileSdk")
            defaultConfig {
                minSdk = version("android-minSdk")
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
}
