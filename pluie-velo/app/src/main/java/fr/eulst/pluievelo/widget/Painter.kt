package fr.eulst.pluievelo.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import fr.eulst.pluievelo.core.WET_RATE
import fr.eulst.pluievelo.data.Snapshot
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object Fmt {
    private val hm = DateTimeFormatter.ofPattern("HH:mm")
    fun time(epochS: Long): String = hm.format(Instant.ofEpochSecond(epochS).atZone(ZoneId.systemDefault()))
}

object Palette {
    const val TEXT = 0xFFF3F6FC.toInt()
    const val MUTED = 0xFF9FADC8.toInt()
    const val GRID = 0x33A9B8D8
    const val DOT = 0xFF4C5C80.toInt()
    const val ACCENT = 0xFF5AA9FF.toInt()

    fun rain(rate: Float): Int = when {
        rate < 1f -> 0xFF7FD6F2.toInt()
        rate < 4f -> 0xFF3D9BFF.toInt()
        rate < 10f -> 0xFF8468FF.toInt()
        else -> 0xFFE8489B.toInt()
    }

    fun level(level: Int): Int = intArrayOf(
        0xFF4CD08A.toInt(), 0xFFA9DB5A.toInt(), 0xFFFFCB4D.toInt(), 0xFFFF9840.toInt(), 0xFFFF5C6C.toInt(),
    )[level.coerceIn(0, 4)]
}

/** Rain bar chart shared by the app screens and the widget. */
object ChartPainter {
    class Label(val fraction: Float, val text: String)

    /** Bar height as a fraction of the plot: square-root scale, full height at 10 mm/h. */
    fun height(rate: Float): Float = min(1f, sqrt(rate / 10f))

    fun draw(
        c: Canvas, left: Float, top: Float, right: Float, bottom: Float,
        rates: FloatArray, probs: FloatArray?, labels: List<Label>, density: Float, scale: Boolean,
    ) {
        if (rates.isEmpty()) return
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Palette.MUTED; textSize = 11f * density }
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Palette.GRID; strokeWidth = density }
        val plotBottom = bottom - 18f * density
        val plotTop = top + 4f * density
        val plotH = plotBottom - plotTop
        // The intensity names sit in their own margin so they never cover the bars.
        val right = if (scale) right - 58f * density else right
        val w = right - left

        if (scale) {
            for ((rate, name) in listOf(1f to "modérée", 4f to "forte", 10f to "très forte")) {
                val y = plotBottom - plotH * height(rate)
                c.drawLine(left, y, right, y, line)
                text.textAlign = Paint.Align.LEFT
                c.drawText(name, right + 5f * density, y + 4f * density, text)
            }
        }
        for (l in labels) {
            val x = left + w * l.fraction
            if (l.fraction > 0.001f) c.drawLine(x, plotTop, x, plotBottom, line)
            text.textAlign = if (l.fraction > 0.93f) Paint.Align.RIGHT else Paint.Align.LEFT
            c.drawText(l.text, x, bottom - 3f * density, text)
        }

        val slot = w / rates.size
        val barW = (slot * 0.68f).coerceAtLeast(density)
        val bar = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF()
        for (i in rates.indices) {
            val cx = left + slot * (i + 0.5f)
            val r = rates[i]
            if (r < WET_RATE) {
                bar.color = Palette.DOT
                val d = min(barW, 3.5f * density)
                rect.set(cx - barW / 2, plotBottom - d, cx + barW / 2, plotBottom)
                c.drawRoundRect(rect, d / 2, d / 2, bar)
            } else {
                val p = probs?.getOrNull(i) ?: 1f
                bar.color = Palette.rain(r)
                bar.alpha = (255 * (0.45f + 0.55f * p.coerceIn(0f, 1f))).toInt()
                val h = (plotH * height(r)).coerceAtLeast(5f * density)
                rect.set(cx - barW / 2, plotBottom - h, cx + barW / 2, plotBottom)
                val rad = min(barW / 2, 3f * density)
                c.drawRoundRect(rect, rad, rad, bar)
            }
        }
    }
}

object WidgetPainter {
    fun render(s: Snapshot?, widthPx: Int, heightPx: Int, density: Float, nowS: Long): Bitmap {
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val w = widthPx.toFloat()
        val h = heightPx.toFloat()
        val d = density
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, w * 0.3f, h, 0xFF1D3A6E.toInt(), 0xFF0E1A33.toInt(), Shader.TileMode.CLAMP)
        }
        c.drawRoundRect(RectF(0f, 0f, w, h), 26f * d, 26f * d, bg)

        val pad = 16f * d
        // Refresh button, top right.
        val bcx = w - pad - 18f * d
        val bcy = pad + 18f * d
        c.drawCircle(bcx, bcy, 18f * d, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF2A4474.toInt() })
        refreshGlyph(c, bcx, bcy, 8.5f * d, d)

        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Palette.TEXT; typeface = Typeface.DEFAULT_BOLD; textSize = 22f * d }
        val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Palette.MUTED; textSize = 13.5f * d }
        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Palette.MUTED; textSize = 12.5f * d; textAlign = Paint.Align.RIGHT }

        if (s == null) {
            c.drawText("Pluie Vélo", pad, pad + 24f * d, title)
            c.drawText("Touchez ↻ pour charger la prévision", pad, pad + 46f * d, sub)
            return bmp
        }

        // Place and time, left of the button.
        val infoRight = bcx - 26f * d
        val place = s.place.ifBlank { "Ma position" }
        c.drawText(ellipsize(place, small, w * 0.34f), infoRight, pad + 14f * d, small)
        val stale = nowS - s.timeS > 40 * 60
        small.color = if (stale) 0xFFFFB05C.toInt() else Palette.MUTED
        c.drawText(Fmt.time(s.timeS), infoRight, pad + 32f * d, small)
        val infoW = maxOf(small.measureText(Fmt.time(s.timeS)), small.measureText(ellipsize(place, small, w * 0.34f)))

        val wetSoon = s.rates.take(12).any { it >= WET_RATE }
        icon(c, pad + 15f * d, pad + 17f * d, 15f * d, d, s.raining, wetSoon)
        val textX = pad + 42f * d
        val maxTitle = infoRight - infoW - 10f * d - textX
        while (title.textSize > 14f * d && title.measureText(s.title) > maxTitle) title.textSize -= d
        c.drawText(s.title, textX, pad + 22f * d, title)
        c.drawText(ellipsize(s.subtitle, sub, w - textX - pad), textX, pad + 44f * d, sub)

        val chartTop = pad + 56f * d
        if (h - pad - chartTop > 34f * d) {
            val n = s.rates.size
            val labels = (0 until n step 6).map { i ->
                ChartPainter.Label(i.toFloat() / n, if (i == 0 && !stale) "Maintenant" else Fmt.time(s.timeS + i * 300L))
            }
            ChartPainter.draw(c, pad, chartTop, w - pad, h - pad + 4f * d, s.rates, s.probs, labels, d, false)
        }
        return bmp
    }

    private fun ellipsize(text: String, p: Paint, max: Float): String {
        if (p.measureText(text) <= max) return text
        var t = text
        while (t.length > 1 && p.measureText("$t…") > max) t = t.dropLast(1)
        return "$t…"
    }

    private fun refreshGlyph(c: Canvas, cx: Float, cy: Float, r: Float, d: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Palette.ACCENT; style = Paint.Style.STROKE; strokeWidth = 2.4f * d; strokeCap = Paint.Cap.ROUND
        }
        c.drawArc(RectF(cx - r, cy - r, cx + r, cy + r), -50f, 300f, false, p)
        // Arrow head at the open end of the arc (angle -50°).
        val a = Math.toRadians(-50.0)
        val ex = cx + r * cos(a).toFloat()
        val ey = cy + r * sin(a).toFloat()
        val head = Path().apply {
            moveTo(ex + 1.5f * d, ey - 5.5f * d)
            lineTo(ex + 1.5f * d, ey + 1.5f * d)
            lineTo(ex - 5.5f * d, ey + 1.5f * d)
            close()
        }
        c.drawPath(head, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Palette.ACCENT })
    }

    /** Sun when dry for the next hour, cloud when rain is on its way, cloud with drops when raining. */
    fun icon(c: Canvas, cx: Float, cy: Float, r: Float, d: Float, raining: Boolean, wetSoon: Boolean) {
        if (!raining && !wetSoon) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFC94D.toInt(); strokeWidth = 2.2f * d; strokeCap = Paint.Cap.ROUND }
            c.drawCircle(cx, cy, r * 0.48f, p)
            for (k in 0 until 8) {
                val a = Math.toRadians(k * 45.0)
                c.drawLine(
                    cx + r * 0.72f * cos(a).toFloat(), cy + r * 0.72f * sin(a).toFloat(),
                    cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat(), p,
                )
            }
            return
        }
        val cloud = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = if (raining) 0xFFB9C6E0.toInt() else 0xFFD9E2F3.toInt() }
        val top = cy - (if (raining) r * 0.35f else 0f)
        c.drawCircle(cx - r * 0.45f, top + r * 0.1f, r * 0.42f, cloud)
        c.drawCircle(cx + r * 0.1f, top - r * 0.2f, r * 0.55f, cloud)
        c.drawCircle(cx + r * 0.6f, top + r * 0.15f, r * 0.36f, cloud)
        c.drawRoundRect(RectF(cx - r * 0.87f, top + r * 0.05f, cx + r * 0.96f, top + r * 0.52f), r * 0.24f, r * 0.24f, cloud)
        if (raining) {
            val drop = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Palette.ACCENT; strokeWidth = 2.2f * d; strokeCap = Paint.Cap.ROUND }
            for (k in -1..1) {
                val x = cx + k * r * 0.5f
                c.drawLine(x + r * 0.08f, top + r * 0.8f, x - r * 0.1f, top + r * 1.25f, drop)
            }
        }
    }
}
