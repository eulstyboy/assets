package fr.eulst.pluievelo.core

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLon(val lat: Double, val lon: Double)

/** Web-Mercator helpers; pixel coordinates are global, for 256 px tiles. */
object Geo {
    const val TILE = 256

    fun worldX(lon: Double, zoom: Int): Double = (lon + 180.0) / 360.0 * (TILE shl zoom)

    fun worldY(lat: Double, zoom: Int): Double {
        val s = sin(Math.toRadians(lat.coerceIn(-85.05, 85.05)))
        return (0.5 - ln((1 + s) / (1 - s)) / (4 * PI)) * (TILE shl zoom)
    }

    fun metersPerPixel(lat: Double, zoom: Int): Double =
        156543.03392 * cos(Math.toRadians(lat)) / (1 shl zoom)

    fun distanceM(a: LatLon, b: LatLon): Double {
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val h = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLon / 2).pow(2)
        return 2 * 6371000.0 * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }

    /** Initial bearing from [a] to [b], degrees clockwise from north. */
    fun bearingDeg(a: LatLon, b: LatLon): Double {
        val la = Math.toRadians(a.lat)
        val lb = Math.toRadians(b.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val y = sin(dLon) * cos(lb)
        val x = cos(la) * sin(lb) - sin(la) * cos(lb) * cos(dLon)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    private val compass = arrayOf("nord", "nord-est", "est", "sud-est", "sud", "sud-ouest", "ouest", "nord-ouest")

    fun compassFr(deg: Double): String = compass[(((deg % 360 + 360) % 360 + 22.5) / 45).toInt() % 8]
}
