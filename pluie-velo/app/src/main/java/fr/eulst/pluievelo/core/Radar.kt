package fr.eulst.pluievelo.core

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/** Rain rate (mm/h) from which a moment counts as "it rains". */
const val WET_RATE = 0.15f

/**
 * Converts RainViewer "Universal Blue" tile colours to reflectivity (dBZ) then rain rate.
 * The scale has one colour per dBZ; it was rebuilt from live tiles (see README).
 */
object RadarPalette {
    // dBZ at segment start, colour at start, colour the segment would reach 5 dBZ later.
    private val segments = listOf(
        Triple(10, 0x88ddee, 0x00a3e0), Triple(15, 0x00a3e0, 0x0077aa),
        Triple(20, 0x0077aa, 0x005588), Triple(25, 0x005588, 0x004460),
        Triple(30, 0xffee00, 0xffaa00), Triple(35, 0xffaa00, 0xff7700),
        Triple(40, 0xff4400, 0xc10000), Triple(45, 0xc10000, 0x440000),
        Triple(50, 0xffaaff, 0xff77ff), Triple(55, 0xff77ff, 0xff44ff),
        Triple(60, 0xff44ff, 0xff11ff),
    )
    private val tableDbz = IntArray(56)
    private val tableRgb = IntArray(56)
    private val cache = HashMap<Int, Float>()

    init {
        var i = 0
        for ((start, from, to) in segments) {
            for (k in 0 until 5) {
                tableDbz[i] = start + k
                tableRgb[i] = lerpRgb(from, to, k / 5.0)
                i++
            }
        }
        tableDbz[i] = 65
        tableRgb[i] = 0xffffff
    }

    private fun lerpRgb(a: Int, b: Int, t: Double): Int {
        fun ch(shift: Int): Int {
            val x = (a shr shift) and 0xff
            val y = (b shr shift) and 0xff
            return (x + (y - x) * t).toInt()
        }
        return (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }

    /** Reflectivity for an ARGB pixel, or null when the pixel carries no echo. */
    fun dbz(argb: Int): Float? {
        val a = argb ushr 24
        if (a == 0) return null
        if (a < 250) {
            // Translucent beige ramp: very weak echoes below 10 dBZ.
            return if (a >= 100) (a - 100) / 10f else -15f + (a - 20) / 5.3f
        }
        val r = (argb shr 16) and 0xff
        val g = (argb shr 8) and 0xff
        val b = argb and 0xff
        var best = 0
        var bestD = Int.MAX_VALUE
        for (i in tableRgb.indices) {
            val c = tableRgb[i]
            val dr = r - ((c shr 16) and 0xff)
            val dg = g - ((c shr 8) and 0xff)
            val db = b - (c and 0xff)
            val d = dr * dr + dg * dg + db * db
            if (d < bestD) {
                bestD = d
                best = i
            }
        }
        // A colour far from every entry is not radar data (e.g. an error tile).
        return if (bestD > 40 * 40 * 3) null else tableDbz[best].toFloat()
    }

    /** Marshall-Palmer Z = 200 R^1.6. Echoes under 8 dBZ are treated as no rain. */
    fun rateFromDbz(dbz: Float): Float {
        if (dbz < 8f) return 0f
        val z = 10.0.pow(dbz / 10.0)
        return min(150.0, (z / 200.0).pow(1 / 1.6)).toFloat()
    }

    @Synchronized
    fun rate(argb: Int): Float = cache.getOrPut(argb) { dbz(argb)?.let(::rateFromDbz) ?: 0f }
}

/** Rain-rate field (mm/h) on a Web-Mercator pixel grid. */
class RadarGrid(
    val zoom: Int,
    val originX: Int,
    val originY: Int,
    val width: Int,
    val height: Int,
    val rate: FloatArray,
    val timeS: Long,
) {
    fun contains(px: Double, py: Double): Boolean =
        px >= originX && py >= originY && px <= originX + width - 1 && py <= originY + height - 1

    /** Bilinear sample at global pixel coordinates, null outside the grid. */
    fun sample(px: Double, py: Double): Float? {
        if (!contains(px, py)) return null
        val x = px - originX
        val y = py - originY
        val x0 = min(floor(x).toInt(), width - 2)
        val y0 = min(floor(y).toInt(), height - 2)
        val fx = (x - x0).toFloat()
        val fy = (y - y0).toFloat()
        val i = y0 * width + x0
        val top = rate[i] * (1 - fx) + rate[i + 1] * fx
        val bottom = rate[i + width] * (1 - fx) + rate[i + width + 1] * fx
        return top * (1 - fy) + bottom * fy
    }

    /** Mean rate and wet fraction in a disc; null when the disc centre is outside the grid. */
    fun disc(px: Double, py: Double, radius: Double): DiscStats? {
        if (!contains(px, py)) return null
        val cx = px - originX
        val cy = py - originY
        val r = max(0.5, radius)
        var sum = 0.0
        var wet = 0
        var n = 0
        for (y in max(0, floor(cy - r).toInt())..min(height - 1, ceil(cy + r).toInt())) {
            for (x in max(0, floor(cx - r).toInt())..min(width - 1, ceil(cx + r).toInt())) {
                if (hypot(x - cx, y - cy) > r + 0.5) continue
                val v = rate[y * width + x]
                sum += v
                if (v >= WET_RATE) wet++
                n++
            }
        }
        if (n == 0) return null
        return DiscStats((sum / n).toFloat(), wet.toFloat() / n)
    }

    fun wetCount(): Int = rate.count { it >= WET_RATE }
}

data class DiscStats(val mean: Float, val wetFraction: Float)

/**
 * Displacement of the rain field in pixels per second (x east, y south).
 * [quality] is 0..1: how well the shifted frames match and how consistent successive pairs are.
 */
data class Motion(val vx: Double, val vy: Double, val quality: Double) {
    fun speedKmh(metersPerPixel: Double): Double = hypot(vx, vy) * metersPerPixel * 3.6

    /** Direction the rain comes FROM, degrees clockwise from north. */
    fun fromDeg(): Double = (Math.toDegrees(kotlin.math.atan2(-vx, vy)) + 360.0) % 360.0
}

object MotionEstimator {
    private const val MAX_SPEED_MS = 33.0
    private const val MIN_WET = 20

    /**
     * Estimates the motion near ([cx], [cy]) from successive frames (oldest first).
     * Returns null when there is too little rain to track.
     */
    fun estimate(frames: List<RadarGrid>, cx: Double, cy: Double, metersPerPixel: Double): Motion? {
        if (frames.size < 2) return null
        val shifts = ArrayList<DoubleArray>() // vx, vy, score
        for (i in 1 until frames.size) {
            val a = frames[i - 1]
            val b = frames[i]
            val dt = (b.timeS - a.timeS).toDouble()
            if (dt <= 0) continue
            val maxShift = ceil(MAX_SPEED_MS * dt / metersPerPixel).toInt().coerceIn(2, 40)
            val r = pairShift(a, b, cx, cy, 110, maxShift) ?: pairShift(a, b, cx, cy, Int.MAX_VALUE, maxShift)
            if (r != null) shifts.add(doubleArrayOf(r[0] / dt, r[1] / dt, r[2]))
        }
        if (shifts.isEmpty()) return null
        var wSum = 0.0
        var vx = 0.0
        var vy = 0.0
        var q = 0.0
        for (s in shifts) {
            val w = s[2] * s[2]
            wSum += w
            vx += w * s[0]
            vy += w * s[1]
            q += w * s[2]
        }
        if (wSum <= 0) return null
        vx /= wSum
        vy /= wSum
        q /= wSum
        // Penalise disagreement between pairs (in pixels per 10 minutes).
        var spread = 0.0
        for (s in shifts) spread += hypot(s[0] - vx, s[1] - vy) * 600
        spread /= shifts.size
        val consistency = 1.0 / (1.0 + spread / 3.0)
        return Motion(vx, vy, (q * consistency).coerceIn(0.0, 1.0))
    }

    /** Best shift (dx, dy, score) such that b(x+dx, y+dy) matches a(x, y), within a window around the centre. */
    private fun pairShift(a: RadarGrid, b: RadarGrid, cx: Double, cy: Double, half: Int, maxShift: Int): DoubleArray? {
        if (a.width != b.width || a.height != b.height || a.originX != b.originX || a.originY != b.originY) return null
        val w = a.width
        val h = a.height
        val x0: Int
        val x1: Int
        val y0: Int
        val y1: Int
        if (half == Int.MAX_VALUE) {
            x0 = 0; x1 = w; y0 = 0; y1 = h
        } else {
            val lx = (cx - a.originX).toInt()
            val ly = (cy - a.originY).toInt()
            x0 = (lx - half).coerceIn(0, w); x1 = (lx + half).coerceIn(0, w)
            y0 = (ly - half).coerceIn(0, h); y1 = (ly + half).coerceIn(0, h)
        }
        if (x1 - x0 < 16 || y1 - y0 < 16) return null
        val fa = transform(a)
        val fb = transform(b)
        var wetA = 0
        var wetB = 0
        for (y in y0 until y1) for (x in x0 until x1) {
            if (fa[y * w + x] > 0f) wetA++
            if (fb[y * w + x] > 0f) wetB++
        }
        if (wetA < MIN_WET || wetB < MIN_WET) return null

        // Coarse search on a 2x reduced grid, then refine at full resolution.
        val ca = reduce(fa, w, h)
        val cb = reduce(fb, w, h)
        val cw = w / 2
        val ch = h / 2
        val cm = (maxShift + 1) / 2
        var best = Triple(0, 0, -1.0)
        for (dy in -cm..cm) for (dx in -cm..cm) {
            val s = ncc(ca, cb, cw, ch, x0 / 2, x1 / 2, y0 / 2, y1 / 2, dx, dy)
            if (s > best.third) best = Triple(dx, dy, s)
        }
        if (best.third <= 0) return null
        val scores = HashMap<Long, Double>()
        fun score(dx: Int, dy: Int): Double =
            scores.getOrPut(dx.toLong() * 100000 + dy) { ncc(fa, fb, w, h, x0, x1, y0, y1, dx, dy) }
        var bx = best.first * 2
        var by = best.second * 2
        var bs = -1.0
        for (dy in best.second * 2 - 2..best.second * 2 + 2) for (dx in best.first * 2 - 2..best.first * 2 + 2) {
            val s = score(dx, dy)
            if (s > bs) {
                bs = s; bx = dx; by = dy
            }
        }
        if (bs < 0.15) return null
        val subX = parabola(score(bx - 1, by), bs, score(bx + 1, by))
        val subY = parabola(score(bx, by - 1), bs, score(bx, by + 1))
        return doubleArrayOf(bx + subX, by + subY, bs)
    }

    private fun parabola(l: Double, c: Double, r: Double): Double {
        val d = l - 2 * c + r
        return if (abs(d) < 1e-9) 0.0 else (0.5 * (l - r) / d).coerceIn(-0.5, 0.5)
    }

    /** Compress the dynamic range so heavy cells do not dominate the match. */
    private fun transform(g: RadarGrid): FloatArray =
        FloatArray(g.rate.size) { i -> g.rate[i].let { if (it < WET_RATE) 0f else sqrt(min(it, 16f)) } }

    private fun reduce(f: FloatArray, w: Int, h: Int): FloatArray {
        val cw = w / 2
        val ch = h / 2
        return FloatArray(cw * ch) { i ->
            val x = (i % cw) * 2
            val y = (i / cw) * 2
            (f[y * w + x] + f[y * w + x + 1] + f[(y + 1) * w + x] + f[(y + 1) * w + x + 1]) / 4f
        }
    }

    /** Normalised cross-correlation of a(window) against b shifted by (dx, dy). */
    private fun ncc(
        a: FloatArray, b: FloatArray, w: Int, h: Int,
        x0: Int, x1: Int, y0: Int, y1: Int, dx: Int, dy: Int,
    ): Double {
        var sab = 0.0
        var saa = 0.0
        var sbb = 0.0
        val ya = max(y0, -dy)
        val yb = min(y1, h - dy)
        val xa = max(x0, -dx)
        val xb = min(x1, w - dx)
        for (y in ya until yb) {
            val ra = y * w
            val rb = (y + dy) * w + dx
            for (x in xa until xb) {
                val va = a[ra + x]
                val vb = b[rb + x]
                sab += va * vb
                saa += va * va
                sbb += vb * vb
            }
        }
        if (saa <= 0 || sbb <= 0) return 0.0
        return sab / sqrt(saa * sbb)
    }
}

/** What the radar extrapolation says for one place and time. */
data class RadarSample(val rate: Float, val areaMean: Float, val wetFraction: Float)

/** Extrapolates the latest radar frame along the estimated motion ("Lagrangian persistence"). */
class RadarNowcast(val latest: RadarGrid, val motion: Motion?, val metersPerPixel: Double) {
    /** True when rain exists in the grid but its motion could not be tracked. */
    val untracked: Boolean = motion == null && latest.wetCount() > 0

    val quality: Double = when {
        motion != null -> motion.quality
        untracked -> 0.2
        else -> 0.7 // nothing on the radar at all: nothing to move
    }

    fun at(p: LatLon, timeS: Long): RadarSample? {
        val dt = max(0L, timeS - latest.timeS).toDouble()
        val px = Geo.worldX(p.lon, latest.zoom)
        val py = Geo.worldY(p.lat, latest.zoom)
        val sx = px - (motion?.vx ?: 0.0) * dt
        val sy = py - (motion?.vy ?: 0.0) * dt
        val travelled = hypot(motion?.vx ?: 0.0, motion?.vy ?: 0.0) * dt
        // Position uncertainty grows with the distance travelled; more when motion is unknown.
        val radius = 1.0 + 0.2 * travelled + (if (untracked) 8.0 * dt / metersPerPixel else 0.0)
        val centre = latest.sample(sx, sy) ?: return null
        val area = latest.disc(sx, sy, radius) ?: return null
        return RadarSample(centre, area.mean, area.wetFraction)
    }
}
