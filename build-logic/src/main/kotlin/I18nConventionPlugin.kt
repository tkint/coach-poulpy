import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Generates a module's text from the translation files in `i18n/` (ADR 0014, ADR 0015) by running
 * `tools/i18n`. Outputs go to the module's `build/generated/i18n`, never committed. Each module asks for
 * what it needs through the `i18n` extension, which also wires the output into the build that reads it.
 */
class I18nConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val tool = project.configurations.create("i18nTool") {
            isCanBeConsumed = false
            isCanBeResolved = true
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
                attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
                attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling.EXTERNAL))
                attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, project.objects.named(TargetJvmEnvironment.STANDARD_JVM))
            }
        }
        project.dependencies.add(tool.name, project.dependencies.project(mapOf("path" to ":tools:i18n")))
        project.extensions.create<I18nExtension>("i18n", project)
    }
}

abstract class I18nExtension @Inject constructor(private val project: Project) {
    /** `values*/strings.xml` from [namespace], added to every variant's resources. */
    fun androidStrings(namespace: String): TaskProvider<GenerateTranslations> {
        val task = register("generateAndroidStrings", "android", listOf(namespace))
        project.extensions.getByType<ApplicationAndroidComponentsExtension>().onVariants { variant ->
            variant.sources.res?.addGeneratedSourceDirectory(task, GenerateTranslations::outputDirectory)
        }
        return task
    }

    /** `<locale>.lproj/Localizable.strings` and `.stringsdict` from [namespace], made before Xcode embeds the framework. */
    fun appleStrings(namespace: String): TaskProvider<GenerateTranslations> {
        val task = register("generateAppleStrings", "apple", listOf(namespace))
        project.tasks.matching { it.name == "embedAndSignAppleFrameworkForXcode" }.configureEach { dependsOn(task) }
        return task
    }

    /** The internal object [objectName] in [packageName], one function per message of [namespace], in `commonMain`. */
    fun kotlinText(namespace: String, packageName: String, objectName: String): TaskProvider<GenerateTranslations> {
        val task = register("generate${objectName}", "kotlin", listOf(namespace, packageName, objectName))
        project.extensions.getByType<KotlinMultiplatformExtension>().sourceSets.getByName("commonMain").kotlin.srcDir(task)
        return task
    }

    /** [structure] with the `catalog` text merged back in, as `exercises.json` in the task's output directory. */
    fun catalog(structure: RegularFile): TaskProvider<GenerateTranslations> =
        register("mergeCatalogText", "catalog", emptyList()) { catalogStructure.set(structure) }

    private fun register(
        name: String,
        command: String,
        arguments: List<String>,
        configure: GenerateTranslations.() -> Unit = {},
    ): TaskProvider<GenerateTranslations> = project.tasks.register<GenerateTranslations>(name) {
        group = "build"
        description = "Generates $command text from the translation files in i18n/"
        val i18n = project.rootProject.layout.projectDirectory.dir("i18n")
        messages.set(i18n.dir("messages"))
        settings.set(i18n.file("project.inlang/settings.json"))
        this.command.set(command)
        this.arguments.set(arguments)
        outputDirectory.set(project.layout.buildDirectory.dir("generated/i18n/$command"))
        classpath = project.configurations.getByName("i18nTool")
        mainClass.set("ch.coachpoulpy.i18n.MainKt")
        javaLauncher.set(
            project.extensions.getByType<JavaToolchainService>().launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) },
        )
        configure()
    }
}

/** One run of `tools/i18n`: `<command> <i18n dir> <arguments> [<catalog structure>] <output>`. */
abstract class GenerateTranslations : JavaExec() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val messages: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val settings: RegularFileProperty

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val catalogStructure: RegularFileProperty

    @get:Input
    abstract val command: Property<String>

    @get:Input
    abstract val arguments: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    override fun exec() {
        val output = outputDirectory.get().asFile
        val target = if (catalogStructure.isPresent) output.resolve("exercises.json") else output
        args = listOf(command.get(), messages.get().asFile.parentFile.path) +
            arguments.get() +
            listOfNotNull(catalogStructure.orNull?.asFile?.path) +
            target.path
        super.exec()
    }
}
