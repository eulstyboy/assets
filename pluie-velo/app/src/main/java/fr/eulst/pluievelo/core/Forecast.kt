package fr.eulst.pluievelo.core

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Radar extrapolation and model consensus for one place and time, and their blend. */
data class Blend(
    val rate: Float,
    val prob: Float,
    val radarRate: Float?,
    val modelRate: Float?,
    val modelsWet: Int,
    val modelsTotal: Int,
)

/**
 * Crosses the radar extrapolation with the forecast models.
 * The radar dominates the first minutes (it sees the actual rain); its weight fades with lead time
 * and with tracking quality, and the models take over after about an hour and a half.
 */
class Blender(private val radar: RadarNowcast?, private val weather: List<PointWeather>, private val nowS: Long) {

    fun nearestWeather(p: LatLon): PointWeather? = weather.minByOrNull { Geo.distanceM(it.point, p) }

    fun radarWeight(leadMin: Double): Double {
        val base = when {
            leadMin <= 10 -> 0.9
            leadMin <= 30 -> 0.9 - 0.3 * (leadMin - 10) / 20
            leadMin <= 60 -> 0.6 - 0.3 * (leadMin - 30) / 30
            leadMin <= 90 -> 0.3 - 0.3 * (leadMin - 60) / 30
            else -> 0.0
        }
        return base * (0.4 + 0.6 * (radar?.quality ?: 0.0))
    }

    fun at(p: LatLon, timeS: Long): Blend {
        val leadMin = max(0.0, (timeS - nowS) / 60.0)

        var modelRate: Float? = null
        var wet = 0
        var total = 0
        var wetShare = 0f
        nearestWeather(p)?.let { w ->
            var sum = 0.0
            var wSum = 0.0
            var wWet = 0.0
            for (m in w.models) {
                val r = m.rateAt(timeS) ?: continue
                sum += r * m.weight
                wSum += m.weight
                total++
                if (r >= WET_RATE) {
                    wet++
                    wWet += m.weight
                }
            }
            if (wSum > 0) {
                modelRate = (sum / wSum).toFloat()
                wetShare = (wWet / wSum).toFloat()
            }
        }

        var radarRate: Float? = null
        var radarProb = 0f
        radar?.at(p, timeS)?.let { s ->
            val spread = (min(1.0, leadMin / 60.0) * 0.6).toFloat()
            radarRate = (1 - spread) * s.rate + spread * s.areaMean
            radarProb = (1 - spread) * (if (s.rate >= WET_RATE) 1f else 0f) + spread * s.wetFraction
        }

        val mr = modelRate
        val rr = radarRate
        val w = when {
            rr == null -> 0.0
            mr == null -> 1.0
            else -> radarWeight(leadMin)
        }.toFloat()
        val rate = w * (rr ?: 0f) + (1 - w) * (mr ?: 0f)
        val prob = w * radarProb + (1 - w) * wetShare
        return Blend(rate, prob.coerceIn(0f, 1f), rr, mr, wet, total)
    }
}

data class Slot(val startS: Long, val durS: Int, val rate: Float, val prob: Float) {
    val wet: Boolean get() = rate >= WET_RATE
}

data class Headline(val title: String, val subtitle: String, val raining: Boolean, val startInMin: Int?, val durationMin: Int?)

enum class Confidence(val label: String) { HIGH("élevée"), MEDIUM("moyenne"), LOW("faible") }

data class SourceLine(val label: String, val detail: String, val mm: Float?)

data class Advice(val level: Int, val title: String, val items: List<String>, val notes: List<String>)

class HereForecast(
    val nowS: Long,
    val place: LatLon,
    val slots: List<Slot>,
    val headline: Headline,
    val mmNextHour: Float,
    val maxRate: Float,
    val confidence: Confidence,
    val confidenceText: String,
    val sources: List<SourceLine>,
    val motionText: String?,
    val tempC: Float?,
    val windKmh: Float?,
    val gustKmh: Float?,
    val advice: Advice,
)

class TripOption(
    val departS: Long,
    val mm: Float,
    val wetMin: Int,
    val maxRate: Float,
    /** Blended rain rate (mm/h) for each minute of the ride. */
    val minutes: FloatArray,
    val advice: Advice,
)

class TripForecast(
    val nowS: Long,
    val distanceKm: Double,
    val durationMin: Int,
    val options: List<TripOption>,
    val bestIndex: Int,
    val windText: String?,
    val confidence: Confidence,
    val confidenceText: String,
    val radarAgeMin: Int?,
)

object Words {
    fun intensity(rate: Float): String = when {
        rate < WET_RATE -> "pas de pluie"
        rate < 1f -> "pluie faible"
        rate < 4f -> "pluie modérée"
        rate < 10f -> "pluie forte"
        else -> "pluie très forte"
    }

    fun duration(min: Int): String = if (min < 60) "$min min" else "${min / 60} h" + (if (min % 60 > 0) " %02d".format(min % 60) else "")
}

object Forecaster {
    const val SLOT_S = 300
    const val HERE_SLOTS = 24

    fun slots(blender: Blender, p: LatLon, nowS: Long, count: Int = HERE_SLOTS): List<Slot> = (0 until count).map { i ->
        val start = nowS + i.toLong() * SLOT_S
        val samples = listOf(50L, 150L, 250L).map { blender.at(p, start + it) }
        Slot(start, SLOT_S, samples.map { it.rate }.average().toFloat(), samples.map { it.prob }.average().toFloat())
    }

    fun headline(slots: List<Slot>): Headline {
        val horizon = Words.duration(slots.size * SLOT_S / 60)
        val firstWet = slots.indexOfFirst { it.wet }
        if (firstWet < 0) return Headline("Pas de pluie", "pendant au moins $horizon", false, null, null)
        // The shower ends at the first dry spell of two slots (10 minutes) or more.
        var end = slots.size
        for (j in firstWet until slots.size) {
            if (!slots[j].wet && (j == slots.lastIndex || !slots[j + 1].wet)) {
                end = j
                break
            }
        }
        val open = end == slots.size
        val dur = (end - firstWet) * SLOT_S / 60
        val peak = slots.subList(firstWet, end).maxOf { it.rate }
        val word = Words.intensity(peak).replaceFirstChar { it.uppercase() }
        return if (firstWet == 0) {
            Headline(word, if (open) "pendant plus de $horizon" else "encore ${Words.duration(dur)} environ", true, 0, if (open) null else dur)
        } else {
            val start = firstWet * SLOT_S / 60
            val sub = Words.intensity(peak) + if (open) ", jusqu'à la fin de la période" else ", pendant ${Words.duration(dur)} environ"
            Headline("Pluie dans $start min", sub, false, start, if (open) null else dur)
        }
    }

    /** How far radar and models agree over the next hour. */
    fun confidence(radar: RadarNowcast?, checks: List<Blend>): Pair<Confidence, String> {
        val both = checks.filter { it.radarRate != null && it.modelRate != null }
        if (radar == null) return Confidence.LOW to "Radar indisponible : prévision fondée sur les modèles seuls."
        if (checks.none { it.modelRate != null }) return Confidence.LOW to "Modèles indisponibles : prévision fondée sur le radar seul."
        if (both.isEmpty()) return Confidence.LOW to "Le radar ne couvre pas cette zone."
        val agree = both.count { (it.radarRate!! >= WET_RATE) == (it.modelsWet * 2 >= it.modelsTotal) }.toDouble() / both.size
        val allDry = both.all { it.radarRate!! < WET_RATE && it.modelsWet == 0 }
        return when {
            allDry -> Confidence.HIGH to "Radar et modèles concordent : rien en vue."
            radar.untracked -> Confidence.LOW to "Déplacement des averses mal établi."
            agree >= 0.8 && radar.quality >= 0.45 -> Confidence.HIGH to "Radar et modèles concordent."
            agree < 0.5 -> Confidence.LOW to "Radar et modèles divergent : averses incertaines."
            else -> Confidence.MEDIUM to "Radar et modèles concordent en partie."
        }
    }

    fun advice(mm: Float, maxRate: Float, apparentC: Float?, gustKmh: Float?, wetRoad: Boolean): Advice {
        val level = when {
            mm < 0.05f -> 0
            mm < 0.3f && maxRate < 1.5f -> 1
            mm < 1.5f && maxRate < 4f -> 2
            mm < 4f && maxRate < 10f -> 3
            else -> 4
        }
        val title = listOf("Rien à prévoir", "Protection légère", "Veste de pluie", "Tenue de pluie complète", "Équipement intégral")[level]
        val items = when (level) {
            0 -> listOf("Tenue habituelle")
            1 -> listOf("Coupe-vent déperlant")
            2 -> listOf("Veste imperméable", "Garde-boue", "Éclairage allumé")
            3 -> listOf("Veste imperméable", "Pantalon de pluie", "Sur-chaussures", "Gants imperméables", "Éclairage allumé")
            else -> listOf("Veste et pantalon de pluie", "Sur-chaussures", "Gants imperméables", "Vêtements de rechange au sec", "Éclairage allumé")
        }.toMutableList()
        val notes = ArrayList<String>()
        if (level == 0 && wetRoad) {
            items.add("Garde-boue")
            notes.add("Chaussée encore mouillée : projections et freinage allongé.")
        }
        if (level == 4) notes.add("Décalez le départ si vous le pouvez.")
        if (apparentC != null && apparentC < 5f) {
            items.add("Gants chauds et tour de cou")
            notes.add("Ressenti ${apparentC.roundToInt()} °C.")
        }
        if (gustKmh != null && gustKmh >= 70f) notes.add("Rafales à ${gustKmh.roundToInt()} km/h : vélo déconseillé.")
        else if (gustKmh != null && gustKmh >= 50f) notes.add("Rafales à ${gustKmh.roundToInt()} km/h : prudence sur les axes exposés.")
        return Advice(level, title, items, notes)
    }

    fun wetRoad(w: PointWeather?, radar: RadarNowcast?, p: LatLon, nowS: Long): Boolean {
        val past = w?.models?.mapNotNull { it.sumMm(nowS - 3600, nowS) }?.maxOrNull() ?: 0f
        val radarNow = radar?.at(p, nowS)?.rate ?: 0f
        return past >= 0.2f || radarNow >= WET_RATE
    }

    fun here(p: LatLon, radar: RadarNowcast?, weather: List<PointWeather>, nowS: Long): HereForecast {
        val blender = Blender(radar, weather, nowS)
        val slots = slots(blender, p, nowS)
        val hour = slots.take(12)
        val mm = hour.sumOf { it.rate * it.durS / 3600.0 }.toFloat()
        val maxRate = hour.maxOf { it.rate }
        val checks = hour.map { blender.at(p, it.startS + it.durS / 2) }
        val (conf, confText) = confidence(radar, checks)
        val w = blender.nearestWeather(p)

        val sources = ArrayList<SourceLine>()
        if (radar != null) {
            val radarMm = hour.sumOf { s -> (radar.at(p, s.startS + s.durS / 2)?.rate ?: 0f) * s.durS / 3600.0 }.toFloat()
            val age = max(0, ((nowS - radar.latest.timeS) / 60).toInt())
            sources.add(SourceLine("Radar extrapolé", "image d'il y a $age min", radarMm))
        } else {
            sources.add(SourceLine("Radar extrapolé", "indisponible", null))
        }
        w?.models?.forEach { m -> sources.add(SourceLine(m.label, "modèle", m.sumMm(nowS, nowS + 3600))) }

        val motionText = radar?.motion?.let { m ->
            val speed = m.speedKmh(radar.metersPerPixel)
            if (speed < 5) "Averses quasi stationnaires"
            else {
                val from = Geo.compassFr(m.fromDeg())
                "Averses venant ${if (from == "ouest" || from == "est") "de l'" else "du "}$from à ${speed.roundToInt()} km/h"
            }
        }

        // Advice for a half-hour outing starting now.
        val half = slots.take(6)
        val adv = advice(
            half.sumOf { it.rate * it.durS / 3600.0 }.toFloat(), half.maxOf { it.rate },
            w?.apparent(nowS), w?.gust(nowS), wetRoad(w, radar, p, nowS),
        )
        return HereForecast(
            nowS, p, slots, headline(slots), mm, maxRate, conf, confText, sources, motionText,
            w?.temp(nowS), w?.wind(nowS), w?.gust(nowS), adv,
        )
    }

    fun trip(route: Route, speedKmh: Double, radar: RadarNowcast?, weather: List<PointWeather>, nowS: Long): TripForecast {
        val blender = Blender(radar, weather, nowS)
        val speedMs = speedKmh / 3.6
        val durationMin = max(1, ceil(route.lengthM / speedMs / 60.0).toInt())
        val mid = route.positionAt(route.lengthM / 2)
        val w = blender.nearestWeather(mid)
        val wetRoad = wetRoad(w, radar, route.points.first(), nowS)

        val options = (0..60 step 5).map { offsetMin ->
            val depart = nowS + offsetMin * 60L
            val minutes = FloatArray(durationMin) { k ->
                val pos = route.positionAt(speedMs * (k + 0.5) * 60)
                blender.at(pos, depart + k * 60L + 30).rate
            }
            val mm = minutes.sum() / 60f
            val maxRate = minutes.maxOrNull() ?: 0f
            val arrive = depart + durationMin * 60L / 2
            TripOption(
                depart, mm, minutes.count { it >= WET_RATE }, maxRate, minutes,
                advice(mm, maxRate, w?.apparent(arrive), w?.gust(arrive), wetRoad),
            )
        }

        // Suggest waiting only when it at least halves the rain received.
        val now = options.first()
        var best = 0
        if (now.mm >= 0.1f) {
            val minMm = options.minOf { it.mm }
            val candidate = options.indexOfFirst { it.mm <= minMm + 0.05f }
            if (options[candidate].mm <= now.mm * 0.5f) best = candidate
        }

        val windText = w?.let {
            val speed = it.wind(nowS) ?: return@let null
            val dir = it.windDir(nowS) ?: return@let null
            if (speed < 10f || route.lengthM < 300) return@let null
            val heading = Geo.bearingDeg(route.points.first(), route.points.last())
            var diff = abs(dir - heading) % 360
            if (diff > 180) diff = 360 - diff
            val side = when {
                diff < 50 -> "de face"
                diff > 130 -> "dans le dos"
                else -> "de côté"
            }
            val gust = it.gust(nowS)?.takeIf { g -> g >= speed + 10 }?.let { g -> ", rafales ${g.roundToInt()} km/h" } ?: ""
            "Vent ${speed.roundToInt()} km/h $side$gust"
        }

        val checks = (0 until min(durationMin, 60) step 5).map { k ->
            blender.at(route.positionAt(speedMs * (k + 0.5) * 60), nowS + k * 60L + 30)
        }
        val (conf, confText) = confidence(radar, checks)
        return TripForecast(
            nowS, route.lengthM / 1000.0, durationMin, options, best, windText, conf, confText,
            radar?.let { max(0, ((nowS - it.latest.timeS) / 60).toInt()) },
        )
    }
}

/** Fetches every source (in parallel, tolerating the loss of one) and builds the forecasts. */
class Engine(fetcher: Fetcher, decoder: TileDecoder) {
    private val radarSource = RadarSource(fetcher, decoder)
    private val modelSource = ModelSource(fetcher)

    private fun load(center: LatLon, points: List<LatLon>, steps: Int): Pair<RadarNowcast?, List<PointWeather>> {
        val pool = Executors.newFixedThreadPool(2)
        try {
            val radar = pool.submit(Callable { runCatching { radarSource.load(center) }.getOrNull() })
            val models = pool.submit(Callable { runCatching { modelSource.load(points, steps) } })
            val r = radar.get()
            val m = models.get()
            if (r == null && m.isFailure) throw m.exceptionOrNull()!!
            return r?.nowcast to (m.getOrNull() ?: emptyList())
        } finally {
            pool.shutdown()
        }
    }

    fun here(p: LatLon, nowS: Long): HereForecast {
        val (radar, weather) = load(p, listOf(p), 10)
        return Forecaster.here(p, radar, weather, nowS)
    }

    fun trip(route: Route, speedKmh: Double, nowS: Long): TripForecast {
        val n = (ceil(route.lengthM / 3000.0).toInt() + 1).coerceIn(2, 8)
        val durationMin = route.lengthM / (speedKmh / 3.6) / 60.0
        val steps = (ceil((durationMin + 75) / 15.0).toInt() + 1).coerceIn(10, 28)
        val (radar, weather) = load(route.positionAt(route.lengthM / 2), route.sample(n), steps)
        return Forecaster.trip(route, speedKmh, radar, weather, nowS)
    }
}
