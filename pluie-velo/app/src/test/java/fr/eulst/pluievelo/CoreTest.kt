package fr.eulst.pluievelo

import fr.eulst.pluievelo.core.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.exp

class CoreTest {
    private fun argb(rgb: Int, a: Int = 255) = (a shl 24) or rgb

    @Test fun paletteDecodesKnownColours() {
        assertEquals(10f, RadarPalette.dbz(argb(0x88ddee))!!, 0f)
        assertEquals(13f, RadarPalette.dbz(argb(0x36bae5))!!, 0f)
        assertEquals(29f, RadarPalette.dbz(argb(0x004768))!!, 0f)
        assertEquals(30f, RadarPalette.dbz(argb(0xffee00))!!, 0f)
        assertEquals(39f, RadarPalette.dbz(argb(0xff8100))!!, 0f)
        assertEquals(44f, RadarPalette.dbz(argb(0xcd0d00))!!, 0f)
        assertEquals(49f, RadarPalette.dbz(argb(0x5d0000))!!, 0f)
        assertEquals(57f, RadarPalette.dbz(argb(0xff62ff))!!, 0f)
        assertNull(RadarPalette.dbz(0))
        assertNull(RadarPalette.dbz(argb(0x00ff00)))
        assertTrue(RadarPalette.rate(argb(0xdacc93, 180)) < WET_RATE)   // weak beige echo
        assertEquals(0.65f, RadarPalette.rateFromDbz(20f), 0.02f)
        assertEquals(2.7f, RadarPalette.rateFromDbz(30f), 0.1f)
        assertEquals(11.5f, RadarPalette.rateFromDbz(40f), 0.3f)
    }

    /** Synthetic showers drifting by a constant shift per frame. */
    private fun frames(dx: Double, dy: Double, n: Int = 4, size: Int = 512): List<RadarGrid> {
        val blobs = listOf(doubleArrayOf(150.0, 300.0, 14.0, 6.0), doubleArrayOf(260.0, 220.0, 9.0, 3.0), doubleArrayOf(330.0, 330.0, 20.0, 2.0), doubleArrayOf(200.0, 150.0, 6.0, 8.0))
        return (0 until n).map { f ->
            val rate = FloatArray(size * size)
            for (y in 0 until size) for (x in 0 until size) {
                var v = 0.0
                for (b in blobs) {
                    val ex = x - (b[0] + dx * f)
                    val ey = y - (b[1] + dy * f)
                    v += b[3] * exp(-(ex * ex + ey * ey) / (2 * b[2] * b[2]))
                }
                rate[y * size + x] = if (v < 0.1) 0f else v.toFloat()
            }
            RadarGrid(7, 16384, 11264, size, size, rate, 1_000_000L + f * 600L)
        }
    }

    @Test fun motionIsRecovered() {
        val fr = frames(3.4, -1.6)
        val m = MotionEstimator.estimate(fr, 16384 + 256.0, 11264 + 256.0, 820.0)!!
        assertEquals(3.4, m.vx * 600, 0.35)
        assertEquals(-1.6, m.vy * 600, 0.35)
        assertTrue("quality ${m.quality}", m.quality > 0.7)
    }

    @Test fun noRainMeansNoMotionButDryNowcast() {
        val empty = (0 until 3).map { RadarGrid(7, 0, 0, 64, 64, FloatArray(64 * 64), 1000L + it * 600) }
        assertNull(MotionEstimator.estimate(empty, 32.0, 32.0, 820.0))
        val nc = RadarNowcast(empty.last(), null, 820.0)
        assertFalse(nc.untracked)
    }

    @Test fun nowcastBringsTheShowerOnTime() {
        val fr = frames(4.0, 0.0)
        val mpp = 820.0
        val m = MotionEstimator.estimate(fr, 16384 + 256.0, 11264 + 256.0, mpp)!!
        val nc = RadarNowcast(fr.last(), m, mpp)
        // First blob centre is at x = 150 + 12 = 162 in the latest frame; a point 16 px east gets its core 40 min later.
        val zoom = 7
        fun lonOf(px: Double) = px / (256 shl zoom) * 360.0 - 180.0
        fun latOf(py: Double): Double {
            val n = Math.PI - 2 * Math.PI * py / (256 shl zoom)
            return Math.toDegrees(Math.atan(Math.sinh(n)))
        }
        val p = LatLon(latOf(11264 + 300.0), lonOf(16384 + 178.0))
        val t0 = fr.last().timeS
        val now = nc.at(p, t0)!!.rate
        val later = nc.at(p, t0 + 40 * 60)!!.rate
        assertTrue("now=$now later=$later", later > now * 1.5 && later > 5f)
    }

    @Test fun openMeteoResponseIsParsed() {
        val body = javaClass.getResource("/openmeteo.json")!!.readText()
        val pts = listOf(LatLon(48.88, 2.43), LatLon(45.76, 4.84))
        val w = ModelSource.parse(body, pts)
        assertEquals(2, w.size)
        assertEquals(5, w[0].models.size)
        assertEquals("Météo-France AROME", w[0].models[0].label)
        val t = w[0].timeS[5]
        assertEquals(0f, w[0].models[0].rateAt(t)!!, 0f)
        assertEquals(14.4f, w[0].temp(t)!!, 0.01f)
        // UK Met Office has 0.1 mm in the 9th step: 0.4 mm/h during that quarter of an hour.
        assertEquals(0.4f, w[0].models[3].rateAt(w[0].timeS[8] - 100)!!, 0.001f)
        assertNull(w[0].models[0].rateAt(w[0].timeS.last() + 1))
    }

    private fun slots(vararg rates: Float) = rates.mapIndexed { i, r -> Slot(i * 300L, 300, r, if (r > 0) 1f else 0f) }

    @Test fun headlines() {
        val dry = Forecaster.headline(slots(*FloatArray(24)))
        assertEquals("Pas de pluie", dry.title)
        assertEquals("pendant au moins 2 h", dry.subtitle)

        val soon = Forecaster.headline(slots(0f, 0f, 0f, 0.5f, 0.8f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f))
        assertEquals("Pluie dans 15 min", soon.title)
        assertEquals(20, soon.durationMin)   // the single dry slot does not end the shower

        val wet = Forecaster.headline(slots(2f, 5f, 3f, 0f, 0f, 0f))
        assertEquals("Pluie forte", wet.title)
        assertEquals("encore 15 min environ", wet.subtitle)
    }

    @Test fun adviceScalesWithRain() {
        assertEquals(0, Forecaster.advice(0f, 0f, 15f, 10f, false).level)
        assertTrue(Forecaster.advice(0f, 0f, 15f, 10f, true).items.contains("Garde-boue"))
        assertEquals(1, Forecaster.advice(0.2f, 0.8f, 15f, 10f, false).level)
        assertEquals(2, Forecaster.advice(0.8f, 2f, 15f, 10f, false).level)
        assertEquals(3, Forecaster.advice(2.5f, 6f, 15f, 10f, false).level)
        assertEquals(4, Forecaster.advice(6f, 12f, 2f, 75f, false).level)
    }

    @Test fun tripPrefersTheDrierDeparture() {
        // A shower crosses a 6 km west-east route during the next 20 minutes, then it is dry.
        val route = Route(listOf(LatLon(48.85, 2.30), LatLon(48.85, 2.3815)))
        val now = 2_000_000L
        val end = LongArray(12) { now - now % 900 + it * 900L }
        val mm = FloatArray(12) { if (end[it] - 900 < now + 1200) 1.0f else 0f }
        val w = PointWeather(route.positionAt(3000.0), listOf(ModelSeries("m", "M", 1.0, end, mm)), end,
            FloatArray(12) { 12f }, FloatArray(12) { 11f }, FloatArray(12) { 20f }, FloatArray(12) { 30f }, FloatArray(12) { 270f })
        val t = Forecaster.trip(route, 18.0, null, listOf(w), now)
        assertEquals(20, t.durationMin)
        assertTrue(t.options[0].mm > 1f)
        assertEquals(0f, t.options[t.bestIndex].mm, 0.05f)
        assertEquals(6, t.bestIndex)   // dry from 27 min on: leave in 30
        assertEquals("Vent 20 km/h dans le dos, rafales 30 km/h", t.windText)
    }

    @Test fun routeAndPlacesParsing() {
        val r = RouteSource.parseRoute("""{"code":"Ok","routes":[{"geometry":{"coordinates":[[2.43,48.88],[2.4301,48.88],[2.44,48.88],[2.44,48.89]],"type":"LineString"}}]}""")
        assertEquals(3, r.points.size)
        assertEquals(1840.0, r.lengthM, 30.0)
        assertEquals(r.points, Route.decode(r.encode())!!.points)
        val p = RouteSource.parsePlaces("""{"features":[{"geometry":{"coordinates":[2.4,48.87]},"properties":{"label":"12 Rue X 75019 Paris"}}]}""")
        assertEquals(Place("12 Rue X 75019 Paris", LatLon(48.87, 2.4)), p[0])
    }
}
