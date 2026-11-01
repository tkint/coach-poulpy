package ch.coachpoulpy.android.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.PoulpyStatus
import ch.coachpoulpy.shared.ShareCard
import java.io.File
import java.util.Locale

/**
 * Draws [card] as a 1080 x 1350 picture, writes it under the cache folder and hands it to the system share sheet
 * through [FileProvider] (see `res/xml/file_paths.xml`). The lines and their order come from the shared [ShareCard];
 * this only draws them, laid out like the finished screen and in the colours of the theme in use ([colors]).
 */
fun shareSessionImage(context: Context, card: ShareCard, chooserTitle: String, colors: ColorScheme) {
    val folder = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(folder, "coach-poulpy-session.png")
    file.outputStream().use { renderShareCard(context, card, colors).compress(Bitmap.CompressFormat.PNG, 100, it) }
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
private const val COLUMNS = 3
private const val TILES_TOP = 670f

internal fun renderShareCard(context: Context, card: ShareCard, colors: ColorScheme): Bitmap {
    val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val ink = colors.onBackground.toArgb()
    val muted = colors.onSurfaceVariant.toArgb()
    fun font(res: Int, fallback: Typeface) = ResourcesCompat.getFont(context, res) ?: fallback
    val display = font(R.font.baloo2_extrabold, Typeface.DEFAULT_BOLD)
    val body = font(R.font.nunito_bold, Typeface.DEFAULT_BOLD)
    val bodyHeavy = font(R.font.nunito_extrabold, Typeface.DEFAULT_BOLD)
    fun paint(color: Int, size: Float, face: Typeface) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        typeface = face
        textAlign = Paint.Align.CENTER
    }

    canvas.drawColor(colors.background.toArgb())

    // Poulpy, as wide as fits in 256 px, keeping the drawing's proportions.
    ContextCompat.getDrawable(context, card.drawing.drawable)?.let { drawing ->
        val ratio = drawing.intrinsicWidth.toFloat() / drawing.intrinsicHeight.coerceAtLeast(1)
        val height = 256f
        val width = height * ratio
        val left = (WIDTH - width) / 2
        drawing.setBounds(left.toInt(), 56, (left + width).toInt(), (56 + height).toInt())
        drawing.draw(canvas)
    }

    // Heading, quote, name and date, as on the finished screen; a line shrinks until it fits on one line.
    val room = WIDTH - 2 * MARGIN
    fun centered(text: String, baseline: Float, paint: Paint, smallest: Float) {
        while (paint.measureText(text) > room && paint.textSize > smallest) paint.textSize -= 2f
        canvas.drawText(TextUtils.ellipsize(text, TextPaint(paint), room, TextUtils.TruncateAt.END).toString(), WIDTH / 2f, baseline, paint)
    }
    centered(card.heading, 384f, paint(ink, 84f, display), 48f)
    centered(card.quote, 450f, paint(colors.primary.toArgb(), 44f, body), 28f)
    centered(card.title, 530f, paint(ink, 60f, bodyHeavy), 36f)
    centered(dateLabel(card.startedAtMillis), 592f, paint(muted, 40f, body), 28f)

    // Tiles like the finished screen's: label over value, on the surface colour with the outline around.
    val columns = card.lines.size.coerceIn(1, COLUMNS)
    val tileWidth = (room - GAP * (columns - 1)) / columns
    val tileHeight = 180f
    val surface = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.surface.toArgb() }
    val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.outline.toArgb(); style = Paint.Style.STROKE; strokeWidth = 3f }
    val label = paint(muted, 32f, bodyHeavy).apply { textAlign = Paint.Align.LEFT; letterSpacing = 0.06f }
    val value = paint(colors.onSurface.toArgb(), 64f, display).apply { textAlign = Paint.Align.LEFT }
    card.lines.forEachIndexed { index, line ->
        val left = MARGIN + (index % columns) * (tileWidth + GAP)
        val top = TILES_TOP + (index / columns) * (tileHeight + GAP)
        val tile = RectF(left, top, left + tileWidth, top + tileHeight)
        canvas.drawRoundRect(tile, 40f, 40f, surface)
        canvas.drawRoundRect(tile, 40f, 40f, outline)
        val fittedLabel = Paint(label)
        while (fittedLabel.measureText(line.label.uppercase(Locale.getDefault())) > tileWidth - 56f && fittedLabel.textSize > 20f) fittedLabel.textSize -= 2f
        canvas.drawText(line.label.uppercase(Locale.getDefault()), left + 28f, top + 68f, fittedLabel)
        val fitted = Paint(value)
        while (fitted.measureText(line.value) > tileWidth - 56f && fitted.textSize > 28f) fitted.textSize -= 4f
        canvas.drawText(line.value, left + 28f, top + 140f, fitted)
    }

    val rows = (card.lines.size + columns - 1) / columns
    drawTentacleCard(canvas, colors, display, bodyHeavy, card.poulpy, top = TILES_TOP + rows * (tileHeight + GAP) + 16f, room = room)

    canvas.drawText(card.appName, WIDTH / 2f, HEIGHT - 56f, paint(colors.primary.toArgb(), 52f, display))
    return bitmap
}

/** The ring with the tentacle this session lit and Poulpy's line about it, on the primary container. */
private fun drawTentacleCard(canvas: Canvas, colors: ColorScheme, display: Typeface, body: Typeface, poulpy: PoulpyStatus, top: Float, room: Float) {
    val ring = 168f
    val height = ring + 64f
    val box = RectF(MARGIN, top, MARGIN + room, top + height)
    canvas.drawRoundRect(box, 40f, 40f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.primaryContainer.toArgb() })

    // Eight rounded segments clockwise from the top, as TentacleRing draws them on screen.
    val stroke = ring * 15f / 200f
    val arc = RectF(MARGIN + 32f + stroke / 2, top + 32f + stroke / 2, MARGIN + 32f + ring - stroke / 2, top + 32f + ring - stroke / 2)
    val segment = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = stroke; strokeCap = Paint.Cap.ROUND }
    for (i in 0 until PoulpyStatus.TENTACLES) {
        segment.color = (if (i < poulpy.tentaclesLit) colors.primary else colors.outline).toArgb()
        canvas.drawArc(arc, i * 45f + 11f - 90f, 23f, false, segment)
    }

    val textLeft = MARGIN + 32f + ring + 36f
    val textWidth = (MARGIN + room - 32f - textLeft).toInt()
    val inkOn = colors.onPrimaryContainer.toArgb()
    val heading = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = inkOn; textSize = 56f; typeface = display }
    val line = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = inkOn; textSize = 36f; typeface = body }
    fun layout(text: String, paint: TextPaint, maxLines: Int) =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, textWidth).setMaxLines(maxLines).setEllipsize(TextUtils.TruncateAt.END).build()
    val headingLayout = layout(poulpy.title, heading, 1)
    val lineLayout = layout(poulpy.line, line, 2)
    val block = headingLayout.height + 8f + lineLayout.height
    canvas.save()
    canvas.translate(textLeft, top + (height - block) / 2)
    headingLayout.draw(canvas)
    canvas.translate(0f, headingLayout.height + 8f)
    lineLayout.draw(canvas)
    canvas.restore()
}

/** "Sat 24 Oct · 18:05", as on the finished screen. */
private fun dateLabel(millis: Long): String = "${dayLabel(millis)} · ${time(millis)}"
