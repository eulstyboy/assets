package fr.eulst.pluievelo

import fr.eulst.pluievelo.core.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import fr.eulst.pluievelo.data.AndroidTileDecoder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.net.HttpURLConnection
import java.net.URL

/** Runs against the real services: `gradle :app:testDebugUnitTest -Dlive=1 --tests '*LiveTest*'`. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class LiveTest {
    private val fetcher = Fetcher { url ->
        var last: Exception? = null
        for (attempt in 0 until 4) {
            try {
                val c = URL(url).openConnection() as HttpURLConnection
                c.connectTimeout = 20000; c.readTimeout = 30000
                c.setRequestProperty("User-Agent", "PluieVelo-test")
                if (c.responseCode !in 200..299) throw RuntimeException("HTTP ${c.responseCode} $url")
                return@Fetcher c.inputStream.use { it.readBytes() }
            } catch (e: Exception) { last = e; Thread.sleep(8000L * (attempt + 1)) }
        }
        throw last!!
    }
    private val decoder = AndroidTileDecoder

    private fun lonOf(px: Double, z: Int) = px / (256 shl z) * 360.0 - 180.0
    private fun latOf(py: Double, z: Int) = Math.toDegrees(Math.atan(Math.sinh(Math.PI - 2 * Math.PI * py / (256 shl z))))

    /** Predict each frame from the ones 30 minutes earlier and compare with what really happened. */
    @Test fun radarBacktest() {
        assumeTrue(System.getProperty("live") == "1")
        val places = (System.getProperty("places") ?: "").ifBlank { "48.86,2.35" }.split(";").map { it.split(",").let { p -> LatLon(p[0].toDouble(), p[1].toDouble()) } }
        for (center in places) {
            val data = RadarSource(fetcher, decoder).load(center, 13) ?: continue
            val f = data.frames
            val mpp = Geo.metersPerPixel(center.lat, 7)
            println("== $center frames=${f.size} wet(latest)=${f.last().wetCount()} motion=${data.nowcast.motion?.let { "%.0f km/h from %s q=%.2f".format(it.speedKmh(mpp), Geo.compassFr(it.fromDeg()), it.quality) }}")
            for (lead in listOf(1, 3, 6)) {
                var hit = 0; var miss = 0; var fa = 0; var pHit = 0; var pMiss = 0; var pFa = 0; var mae = 0.0; var pMae = 0.0; var n = 0
                for (k in (3 + lead) until f.size) {
                    val base = k - lead
                    val hist = f.subList(base - 3, base + 1)
                    val cx = Geo.worldX(center.lon, 7); val cy = Geo.worldY(center.lat, 7)
                    val m = MotionEstimator.estimate(hist, cx, cy, mpp)
                    val nc = RadarNowcast(f[base], m, mpp)
                    val truth = f[k]
                    // Score on the central 160x160 px (about 130 km) around the place.
                    for (y in -80 until 80 step 2) for (x in -80 until 80 step 2) {
                        val px = cx + x; val py = cy + y
                        val obs = truth.sample(px, py) ?: continue
                        val pred = nc.at(LatLon(latOf(py, 7), lonOf(px, 7)), truth.timeS)?.rate ?: continue
                        val pers = f[base].sample(px, py) ?: continue
                        val o = obs >= WET_RATE
                        if (o && pred >= WET_RATE) hit++ else if (o) miss++ else if (pred >= WET_RATE) fa++
                        if (o && pers >= WET_RATE) pHit++ else if (o) pMiss++ else if (pers >= WET_RATE) pFa++
                        mae += Math.abs(pred - obs); pMae += Math.abs(pers - obs); n++
                    }
                }
                fun csi(h: Int, m: Int, f: Int) = if (h + m + f == 0) Double.NaN else h.toDouble() / (h + m + f)
                println("  lead ${lead * 10} min: CSI nowcast=%.2f  persistence=%.2f   MAE nowcast=%.3f persistence=%.3f  (wet obs=%d)".format(csi(hit, miss, fa), csi(pHit, pMiss, pFa), mae / n, pMae / n, hit + miss))
            }
        }
    }

    @Test fun fullForecast() {
        assumeTrue(System.getProperty("live") == "1")
        val engine = Engine(fetcher, decoder)
        val now = System.currentTimeMillis() / 1000
        val p = (System.getProperty("places") ?: "").ifBlank { "48.884,2.433" }.split(";")[0].split(",").let { LatLon(it[0].toDouble(), it[1].toDouble()) }
        val h = engine.here(p, now)
        println("HERE ${h.headline.title} — ${h.headline.subtitle} | 1h=${h.mmNextHour} mm max=${h.maxRate} | ${h.confidence} ${h.confidenceText} | ${h.motionText}")
        println("  slots " + h.slots.joinToString(" ") { "%.1f".format(it.rate) })
        h.sources.forEach { println("  ${it.label} (${it.detail}): ${it.mm}") }
        println("  conseil: ${h.advice.title} ${h.advice.items} ${h.advice.notes}")
        val rs = RouteSource(fetcher)
        val to = rs.search("Place de la République Paris").first()
        println("  dest ${to.label} ; ville=${rs.townName(p)}")
        val route = rs.bikeRoute(p, to.point)
        val t = engine.trip(route, 18.0, now)
        println("TRIP %.1f km %d min | best=+%d min | %s | %s %s | radar age %s".format(t.distanceKm, t.durationMin, t.bestIndex * 5, t.windText, t.confidence, t.confidenceText, t.radarAgeMin))
        t.options.forEachIndexed { i, o -> println("  +${i * 5} min: %.2f mm, %d min wet, max %.1f → ${o.advice.title}".format(o.mm, o.wetMin, o.maxRate)) }
    }
}
