package fr.eulst.pluievelo.data

import android.content.Context
import fr.eulst.pluievelo.core.HereForecast
import fr.eulst.pluievelo.core.LatLon
import fr.eulst.pluievelo.core.Place
import fr.eulst.pluievelo.core.Route
import org.json.JSONArray
import org.json.JSONObject

data class SavedTrip(val name: String, val from: Place?, val to: Place, val route: Route?)

/** What the home-screen widget shows; kept on disk so it survives process death. */
class Snapshot(
    val title: String,
    val subtitle: String,
    val place: String,
    val timeS: Long,
    val raining: Boolean,
    val rates: FloatArray,
    val probs: FloatArray,
) {
    fun toJson(): String = JSONObject()
        .put("title", title).put("subtitle", subtitle).put("place", place).put("time", timeS).put("raining", raining)
        .put("rates", JSONArray(rates.map { it.toDouble() })).put("probs", JSONArray(probs.map { it.toDouble() }))
        .toString()

    companion object {
        fun of(f: HereForecast, place: String) = Snapshot(
            f.headline.title, f.headline.subtitle, place, f.nowS, f.slots.firstOrNull()?.wet == true,
            FloatArray(f.slots.size) { f.slots[it].rate }, FloatArray(f.slots.size) { f.slots[it].prob },
        )

        fun fromJson(s: String): Snapshot? = runCatching {
            val o = JSONObject(s)
            val r = o.getJSONArray("rates")
            val p = o.getJSONArray("probs")
            Snapshot(
                o.getString("title"), o.getString("subtitle"), o.getString("place"), o.getLong("time"), o.getBoolean("raining"),
                FloatArray(r.length()) { r.getDouble(it).toFloat() }, FloatArray(p.length()) { p.getDouble(it).toFloat() },
            )
        }.getOrNull()
    }
}

class Store(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("pluie", Context.MODE_PRIVATE)

    var speedKmh: Int
        get() = prefs.getInt("speed", 18)
        set(v) = prefs.edit().putInt("speed", v).apply()

    var alerts: Boolean
        get() = prefs.getBoolean("alerts", true)
        set(v) = prefs.edit().putBoolean("alerts", v).apply()

    var lastAlertS: Long
        get() = prefs.getLong("lastAlert", 0)
        set(v) = prefs.edit().putLong("lastAlert", v).apply()

    var lastPlace: Place?
        get() {
            if (!prefs.contains("lat")) return null
            return Place(
                prefs.getString("town", "") ?: "",
                LatLon(prefs.getFloat("lat", 0f).toDouble(), prefs.getFloat("lon", 0f).toDouble()),
            )
        }
        set(v) {
            if (v == null) return
            prefs.edit().putFloat("lat", v.point.lat.toFloat()).putFloat("lon", v.point.lon.toFloat()).putString("town", v.label).apply()
        }

    var snapshot: Snapshot?
        get() = prefs.getString("snapshot", null)?.let(Snapshot::fromJson)
        set(v) = prefs.edit().putString("snapshot", v?.toJson()).apply()

    var trips: List<SavedTrip>
        get() = runCatching {
            val a = JSONArray(prefs.getString("trips", "[]"))
            (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                SavedTrip(o.getString("name"), o.optJSONObject("from")?.let(::place), place(o.getJSONObject("to")), o.optString("route").let(Route::decode))
            }
        }.getOrDefault(emptyList())
        set(v) {
            val a = JSONArray()
            for (t in v) {
                val o = JSONObject().put("name", t.name).put("to", json(t.to))
                t.from?.let { o.put("from", json(it)) }
                t.route?.let { o.put("route", it.encode()) }
                a.put(o)
            }
            prefs.edit().putString("trips", a.toString()).apply()
        }

    private fun json(p: Place) = JSONObject().put("label", p.label).put("lat", p.point.lat).put("lon", p.point.lon)
    private fun place(o: JSONObject) = Place(o.getString("label"), LatLon(o.getDouble("lat"), o.getDouble("lon")))
}
