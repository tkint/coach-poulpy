import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Same as the KMP library convention, plus a JavaScript target, for the modules the
 * website reuses so guided sessions run the same engine in the browser.
 */
class KmpWebLibraryConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.apply("coachpoulpy.kmp-library")

        extensions.configure<KotlinMultiplatformExtension> {
            js {
                nodejs()
            }
        }
    }
}
