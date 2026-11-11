package fr.eulst.pluievelo.core

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.math.floor
import kotlin.math.roundToInt

fun interface Fetcher {
    /** Blocking HTTP GET; throws on network errors and non-2xx answers. */
    fun get(url: String): ByteArray
}

class DecodedImage(val width: Int, val height: Int, val argb: IntArray)

fun interface TileDecoder {
    fun decode(png: ByteArray): DecodedImage?
}

private fun fmt(v: Double): String = String.format(Locale.US, "%.5f", v)
private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

// ---------------------------------------------------------------------------------------------
// Radar (RainViewer: composite of national radars, one frame every 10 minutes)
// ---------------------------------------------------------------------------------------------

class RadarData(val frames: List<RadarGrid>, val nowcast: RadarNowcast, val center: LatLon)

class RadarSource(private val fetcher: Fetcher, private val decoder: TileDecoder) {
    companion object {
        const val ZOOM = 7
        const val INDEX_URL = "https://api.rainviewer.com/public/weather-maps.json"
    }

    /** Loads the last [frameCount] frames as a 2x2 tile mosaic centred on [center]. */
    fun load(center: LatLon, frameCount: Int = 4): RadarData? {
        val index = JSONObject(String(fetcher.get(INDEX_URL)))
        val host = index.getString("host")
        val past = index.getJSONObject("radar").getJSONArray("past")
        val entries = (0 until past.length()).map { past.getJSONObject(it) }.takeLast(frameCount)
        if (entries.isEmpty()) return null

        val cx = Geo.worldX(center.lon, ZOOM)
        val cy = Geo.worldY(center.lat, ZOOM)
        val tx0 = floor(cx / Geo.TILE - 0.5).toInt()
        val ty0 = floor(cy / Geo.TILE - 0.5).toInt()
        val size = Geo.TILE * 2

        val pool = Executors.newFixedThreadPool(6)
        try {
            val jobs = entries.map { e ->
                val path = e.getString("path")
                val time = e.getLong("time")
                val tiles = (0 until 4).map { k ->
                    val tx = tx0 + k % 2
                    val ty = ty0 + k / 2
                    pool.submit(Callable {
                        runCatching {
                            decoder.decode(fetcher.get("$host$path/${Geo.TILE}/$ZOOM/$tx/$ty/2/0_0.png"))
                        }.getOrNull()
                    })
                }
                time to tiles
            }
            val frames = jobs.mapNotNull { (time, tiles) ->
                val rate = FloatArray(size * size)
                for ((k, job) in tiles.withIndex()) {
                    val img = job.get() ?: return@mapNotNull null
                    if (img.width != Geo.TILE || img.height != Geo.TILE) return@mapNotNull null
                    val ox = (k % 2) * Geo.TILE
                    val oy = (k / 2) * Geo.TILE
                    for (y in 0 until Geo.TILE) for (x in 0 until Geo.TILE) {
                        rate[(oy + y) * size + ox + x] = RadarPalette.rate(img.argb[y * Geo.TILE + x])
                    }
                }
                RadarGrid(ZOOM, tx0 * Geo.TILE, ty0 * Geo.TILE, size, size, rate, time)
            }.sortedBy { it.timeS }
            if (frames.isEmpty()) return null
            val mpp = Geo.metersPerPixel(center.lat, ZOOM)
            val motion = MotionEstimator.estimate(frames, cx, cy, mpp)
            return RadarData(frames, RadarNowcast(frames.last(), motion, mpp), center)
        } finally {
            pool.shutdown()
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Forecast models (Open-Meteo, 15-minute steps)
// ---------------------------------------------------------------------------------------------

/** One model's precipitation: [precipMm] is the amount fallen in the 15 minutes ending at [endS]. */
class ModelSeries(val id: String, val label: String, val weight: Double, val endS: LongArray, val precipMm: FloatArray) {
    /** Rain rate (mm/h) at [timeS], or null when this model has no value there. */
    fun rateAt(timeS: Long): Float? {
        for (i in endS.indices) {
            if (timeS > endS[i] - 900 && timeS <= endS[i]) {
                val v = precipMm[i]
                return if (v.isNaN()) null else v * 4f
            }
        }
        return null
    }

    /** Millimetres between two instants, or null when the model covers none of it. */
    fun sumMm(fromS: Long, toS: Long): Float? {
        var sum = 0f
        var any = false
        for (i in endS.indices) {
            val a = maxOf(fromS, endS[i] - 900)
            val b = minOf(toS, endS[i])
            if (b > a && !precipMm[i].isNaN()) {
                sum += precipMm[i] * (b - a) / 900f
                any = true
            }
        }
        return if (any) sum else null
    }
}

class PointWeather(
    val point: LatLon,
    val models: List<ModelSeries>,
    val timeS: LongArray,
    val tempC: FloatArray,
    val apparentC: FloatArray,
    val windKmh: FloatArray,
    val gustKmh: FloatArray,
    val windDirDeg: FloatArray,
) {
    private fun idx(t: Long): Int? {
        if (timeS.isEmpty()) return null
        var best = 0
        for (i in timeS.indices) if (kotlin.math.abs(timeS[i] - t) < kotlin.math.abs(timeS[best] - t)) best = i
        return best
    }

    private fun pick(a: FloatArray, t: Long): Float? = idx(t)?.let { a.getOrNull(it) }?.takeIf { !it.isNaN() }
    fun temp(t: Long) = pick(tempC, t)
    fun apparent(t: Long) = pick(apparentC, t)
    fun wind(t: Long) = pick(windKmh, t)
    fun gust(t: Long) = pick(gustKmh, t)
    fun windDir(t: Long) = pick(windDirDeg, t)
}

class ModelSource(private val fetcher: Fetcher) {
    class ModelDef(val id: String, val label: String, val weight: Double)

    companion object {
        /** High-resolution models with native 15-minute output weigh more than coarse hourly ones. */
        val MODELS = listOf(
            ModelDef("meteofrance_seamless", "Météo-France AROME", 3.0),
            ModelDef("icon_seamless", "DWD ICON", 2.0),
            ModelDef("knmi_seamless", "KNMI Harmonie", 1.0),
            ModelDef("ukmo_seamless", "UK Met Office", 1.0),
            ModelDef("ecmwf_ifs025", "ECMWF IFS", 1.0),
        )
        private const val VARS =
            "precipitation,temperature_2m,apparent_temperature,wind_speed_10m,wind_gusts_10m,wind_direction_10m"

        fun url(points: List<LatLon>, steps: Int): String =
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=" + points.joinToString(",") { fmt(it.lat) } +
                "&longitude=" + points.joinToString(",") { fmt(it.lon) } +
                "&minutely_15=$VARS&past_minutely_15=4&forecast_minutely_15=$steps" +
                "&models=" + MODELS.joinToString(",") { it.id } +
                "&timeformat=unixtime&timezone=GMT"

        fun parse(body: String, points: List<LatLon>): List<PointWeather> {
            val trimmed = body.trim()
            val arr = if (trimmed.startsWith("[")) JSONArray(trimmed) else JSONArray().put(JSONObject(trimmed))
            return (0 until arr.length()).map { i ->
                val m = arr.getJSONObject(i).getJSONObject("minutely_15")
                val t = m.getJSONArray("time")
                val time = LongArray(t.length()) { t.getLong(it) }
                fun floats(key: String): FloatArray? {
                    val a = m.optJSONArray(key) ?: return null
                    val out = FloatArray(a.length()) { if (a.isNull(it)) Float.NaN else a.getDouble(it).toFloat() }
                    return if (out.all { it.isNaN() }) null else out
                }
                val models = MODELS.mapNotNull { d ->
                    floats("precipitation_${d.id}")?.let { ModelSeries(d.id, d.label, d.weight, time, it) }
                }
                fun first(name: String): FloatArray =
                    MODELS.firstNotNullOfOrNull { floats("${name}_${it.id}") } ?: floats(name) ?: FloatArray(time.size) { Float.NaN }
                PointWeather(
                    points.getOrElse(i) { points.last() }, models, time,
                    first("temperature_2m"), first("apparent_temperature"),
                    first("wind_speed_10m"), first("wind_gusts_10m"), first("wind_direction_10m"),
                )
            }
        }
    }

    fun load(points: List<LatLon>, steps: Int = 16): List<PointWeather> =
        parse(String(fetcher.get(url(points, steps))), points)
}

// ---------------------------------------------------------------------------------------------
// Bike route (OSRM on routing.openstreetmap.de) and French address search (Géoplateforme / BAN)
// ---------------------------------------------------------------------------------------------

class Route(val points: List<LatLon>) {
    val cumulativeM: DoubleArray = DoubleArray(points.size).also {
        for (i in 1 until points.size) it[i] = it[i - 1] + Geo.distanceM(points[i - 1], points[i])
    }
    val lengthM: Double get() = cumulativeM.lastOrNull() ?: 0.0

    fun positionAt(distanceM: Double): LatLon {
        if (points.size == 1 || distanceM <= 0) return points.first()
        if (distanceM >= lengthM) return points.last()
        var i = cumulativeM.binarySearch(distanceM)
        if (i < 0) i = -i - 2
        i = i.coerceIn(0, points.size - 2)
        val seg = cumulativeM[i + 1] - cumulativeM[i]
        val f = if (seg <= 0) 0.0 else (distanceM - cumulativeM[i]) / seg
        val a = points[i]
        val b = points[i + 1]
        return LatLon(a.lat + (b.lat - a.lat) * f, a.lon + (b.lon - a.lon) * f)
    }

    /** [n] points spread evenly along the route, ends included. */
    fun sample(n: Int): List<LatLon> =
        if (n <= 1) listOf(positionAt(lengthM / 2)) else (0 until n).map { positionAt(lengthM * it / (n - 1)) }

    fun encode(): String = points.joinToString(";") { "${fmt(it.lat)},${fmt(it.lon)}" }

    companion object {
        fun decode(s: String): Route? = runCatching {
            Route(s.split(";").map { p -> p.split(",").let { LatLon(it[0].toDouble(), it[1].toDouble()) } })
        }.getOrNull()?.takeIf { it.points.isNotEmpty() }
    }
}

data class Place(val label: String, val point: LatLon)

class RouteSource(private val fetcher: Fetcher) {
    fun bikeRoute(from: LatLon, to: LatLon): Route {
        val url = "https://routing.openstreetmap.de/routed-bike/route/v1/driving/" +
            "${fmt(from.lon)},${fmt(from.lat)};${fmt(to.lon)},${fmt(to.lat)}?overview=full&geometries=geojson"
        return parseRoute(String(fetcher.get(url)))
    }

    fun search(query: String, near: LatLon? = null): List<Place> {
        var url = "https://data.geopf.fr/geocodage/search?q=${enc(query)}&limit=6&autocomplete=1"
        if (near != null) url += "&lat=${fmt(near.lat)}&lon=${fmt(near.lon)}"
        return parsePlaces(String(fetcher.get(url)))
    }

    /** Short place name (town) for a position, or null. */
    fun townName(p: LatLon): String? = runCatching {
        val body = String(fetcher.get("https://data.geopf.fr/geocodage/reverse?lon=${fmt(p.lon)}&lat=${fmt(p.lat)}&limit=1"))
        val f = JSONObject(body).getJSONArray("features")
        if (f.length() == 0) null else f.getJSONObject(0).getJSONObject("properties").optString("city").ifBlank { null }
    }.getOrNull()

    companion object {
        fun parseRoute(body: String): Route {
            val o = JSONObject(body)
            if (o.optString("code") != "Ok") error("Itinéraire introuvable")
            val coords = o.getJSONArray("routes").getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates")
            // Keep roughly one point every 30 m: enough for rain, small enough to store.
            val all = (0 until coords.length()).map { coords.getJSONArray(it).let { c -> LatLon(c.getDouble(1), c.getDouble(0)) } }
            val kept = ArrayList<LatLon>()
            for ((i, p) in all.withIndex()) {
                if (i == 0 || i == all.lastIndex || Geo.distanceM(kept.last(), p) >= 30) kept.add(p)
            }
            return Route(kept)
        }

        fun parsePlaces(body: String): List<Place> {
            val f = JSONObject(body).getJSONArray("features")
            return (0 until f.length()).map {
                val o = f.getJSONObject(it)
                val c = o.getJSONObject("geometry").getJSONArray("coordinates")
                Place(o.getJSONObject("properties").getString("label"), LatLon(c.getDouble(1), c.getDouble(0)))
            }
        }
    }
}

fun Float.round1(): String = String.format(Locale.FRANCE, "%.1f", this)
fun Double.roundInt(): Int = this.roundToInt()
