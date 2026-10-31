package ch.coachpoulpy.android.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.format.DateFormat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.ShareCard
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Draws [card] as a 1080 x 1350 picture, writes it under the cache folder and hands it to the system share sheet
 * through [FileProvider] (see `res/xml/file_paths.xml`). The lines and their order come from the shared [ShareCard];
 * this only draws them, on the light colours whatever the theme, since the picture leaves the phone.
 */
fun shareSessionImage(context: Context, card: ShareCard, chooserTitle: String) {
    val folder = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(folder, "coach-poulpy-session.png")
    file.outputStream().use { renderShareCard(context, card).compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val send = Intent(Intent.ACTION_SEND)
        .setType("image/png")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(send, chooserTitle))
}

private const val WIDTH = 1080
private const val HEIGHT = 1350
private const val MARGIN = 88f
private const val GAP = 24f

internal fun renderShareCard(context: Context, card: ShareCard): Bitmap {
    val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val ink = 0xFF082F4A.toInt()
    val muted = 0xFF374555.toInt()
    fun font(res: Int, fallback: Typeface) = ResourcesCompat.getFont(context, res) ?: fallback
    val display = font(R.font.baloo2_extrabold, Typeface.DEFAULT_BOLD)
    val body = font(R.font.nunito_bold, Typeface.DEFAULT_BOLD)
    fun paint(color: Int, size: Float, face: Typeface) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        typeface = face
        textAlign = Paint.Align.CENTER
    }

    canvas.drawColor(0xFFDDF0FB.toInt())

    // Poulpy, as wide as fits in 256 px, keeping the drawing's proportions.
    ContextCompat.getDrawable(context, card.drawing.drawable)?.let { drawing ->
        val ratio = drawing.intrinsicWidth.toFloat() / drawing.intrinsicHeight.coerceAtLeast(1)
        val height = 256f
        val width = height * ratio
        val left = (WIDTH - width) / 2
        drawing.setBounds(left.toInt(), 56, (left + width).toInt(), (56 + height).toInt())
        drawing.draw(canvas)
    }

    // The title shrinks until it fits on one line.
    val title = paint(ink, 88f, display)
    val room = WIDTH - 2 * MARGIN
    while (title.measureText(card.title) > room && title.textSize > 44f) title.textSize -= 4f
    val titleText = android.text.TextUtils.ellipsize(card.title, android.text.TextPaint(title), room, android.text.TextUtils.TruncateAt.END).toString()
    canvas.drawText(titleText, WIDTH / 2f, 400f, title)
    canvas.drawText(dateLabel(card.startedAtMillis), WIDTH / 2f, 470f, paint(muted, 44f, body))

    // Two columns of tiles, label over value.
    val tileWidth = (room - GAP) / 2
    val tileHeight = 180f
    val surface = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
    val label = paint(muted, 36f, body).apply { textAlign = Paint.Align.LEFT; letterSpacing = 0.06f }
    val value = paint(ink, 64f, display).apply { textAlign = Paint.Align.LEFT }
    card.lines.forEachIndexed { index, line ->
        val left = MARGIN + (index % 2) * (tileWidth + GAP)
        val top = 540f + (index / 2) * (tileHeight + GAP)
        canvas.drawRoundRect(RectF(left, top, left + tileWidth, top + tileHeight), 40f, 40f, surface)
        canvas.drawText(line.label.uppercase(Locale.getDefault()), left + 36f, top + 68f, label)
        val fitted = Paint(value)
        while (fitted.measureText(line.value) > tileWidth - 72f && fitted.textSize > 32f) fitted.textSize -= 4f
        canvas.drawText(line.value, left + 36f, top + 140f, fitted)
    }

    canvas.drawText(card.appName, WIDTH / 2f, HEIGHT - 56f, paint(ink, 52f, display))
    return bitmap
}

/** "Saturday 24 October", in the user's language. */
private fun dateLabel(millis: Long): String =
    DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEEdMMMM"), Locale.getDefault())
        .format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))
