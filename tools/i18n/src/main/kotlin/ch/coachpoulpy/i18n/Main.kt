package ch.coachpoulpy.i18n

import java.io.File
import kotlin.system.exitProcess

private const val USAGE = """Usage, from the Gradle tasks the coachpoulpy.i18n plugin registers:
  android <i18n dir> <namespace> <res dir>
  apple   <i18n dir> <namespace> <output dir>
  kotlin  <i18n dir> <namespace> <package> <object> <source dir>
  catalog <i18n dir> <catalog structure json> <merged json>"""

fun main(args: Array<String>) {
    try {
        val translations = Translations.read(File(args.getOrNull(1) ?: usage()))
        when (args[0]) {
            "android" -> Android.write(translations, args.at(2), File(args.at(3)))
            "apple" -> Apple.write(translations, args.at(2), File(args.at(3)))
            "kotlin" -> Kotlin.write(translations, args.at(2), args.at(3), args.at(4), File(args.at(5)))
            "catalog" -> CatalogText.write(translations, File(args.at(2)), File(args.at(3)))
            else -> usage()
        }
    } catch (e: I18nException) {
        System.err.println("Translations: ${e.message}")
        exitProcess(1)
    }
}

private fun Array<String>.at(index: Int) = getOrNull(index) ?: usage()

private fun usage(): Nothing {
    System.err.println(USAGE)
    exitProcess(2)
}
