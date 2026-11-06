package ch.coachpoulpy.shared

/**
 * The faces of Poulpy a person can put on the home screen. [id] is lower case and names the launcher alias on
 * Android and the icon set on iOS, so it never changes once shipped. The mood or pose behind each one is drawn by
 * `design/build-poulpy.mjs`; picking an icon never changes Poulpy's mood in the app.
 */
enum class AppIcon(val id: String) {
    HAPPY("happy"),
    PROUD("proud"),
    ANNOYED("annoyed"),
    GRUMPY("grumpy"),
    CHAMPION("champion"),
    COCKY("cocky"),
}

/** The icons in the order Settings lists them. The standard icon comes first and is the one a fresh install shows. */
object AppIcons {
    val all: List<AppIcon> = AppIcon.entries.toList()
    val standard: AppIcon = AppIcon.HAPPY

    fun fromId(id: String?): AppIcon = all.firstOrNull { it.id == id } ?: standard
}
