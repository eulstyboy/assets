package fr.eulst.pluievelo

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.github.takahirom.roborazzi.captureRoboImage
import fr.eulst.pluievelo.core.*
import fr.eulst.pluievelo.data.SavedTrip
import fr.eulst.pluievelo.data.Snapshot
import fr.eulst.pluievelo.ui.*
import fr.eulst.pluievelo.widget.WidgetPainter
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.math.exp

/** Renders the widget and the screens to PNG files: `gradle :app:testDebugUnitTest -Dshots=1 --tests '*ShotsTest*'`. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h1500dp-xhdpi")
class ShotsTest {
    private val dir = File("build/shots").apply { mkdirs() }
    private val now = 1_794_415_000L
    private val place = LatLon(48.884, 2.433)

    /** A band of showers west of the place, moving east at about 40 km/h. */
    private fun scenario(): Pair<RadarNowcast, List<PointWeather>> {
        val size = 512
        val cx = Geo.worldX(place.lon, 7)
        val cy = Geo.worldY(place.lat, 7)
        val ox = (cx - 256).toInt()
        val oy = (cy - 256).toInt()
        val frames = (0 until 4).map { f ->
            val shift = 8.2 * (f - 3)
            val rate = FloatArray(size * size)
            for (y in 0 until size) for (x in 0 until size) {
                var v = 0.0
                for (b in listOf(doubleArrayOf(232.0, 258.0, 9.0, 5.0), doubleArrayOf(205.0, 250.0, 7.0, 9.0), doubleArrayOf(150.0, 300.0, 12.0, 2.0))) {
                    val ex = x - (b[0] + shift)
                    val ey = y - b[1]
                    v += b[3] * exp(-(ex * ex + ey * ey * 0.25) / (2 * b[2] * b[2]))
                }
                rate[y * size + x] = if (v < 0.1) 0f else v.toFloat()
            }
            RadarGrid(7, ox, oy, size, size, rate, now - 420 - (3 - f) * 600L)
        }
        val mpp = Geo.metersPerPixel(place.lat, 7)
        val nowcast = RadarNowcast(frames.last(), MotionEstimator.estimate(frames, cx, cy, mpp), mpp)
        val end = LongArray(16) { now - now % 900 + (it - 3) * 900L }
        fun series(id: String, label: String, w: Double, vararg mm: Float) =
            ModelSeries(id, label, w, end, FloatArray(16) { mm.getOrElse(it - 4) { 0f } })
        val weather = PointWeather(
            place,
            listOf(
                series("a", "Météo-France AROME", 3.0, 0f, 0.3f, 0.9f, 0.5f, 0.1f),
                series("b", "DWD ICON", 2.0, 0f, 0.1f, 0.7f, 0.8f, 0.2f),
                series("c", "KNMI Harmonie", 1.0, 0f, 0f, 0.4f, 0.4f, 0.3f),
                series("d", "UK Met Office", 1.0, 0f, 0.2f, 0.5f, 0.2f),
                series("e", "ECMWF IFS", 1.0, 0.1f, 0.1f, 0.2f, 0.2f, 0.1f),
            ),
            end, FloatArray(16) { 11f }, FloatArray(16) { 9f }, FloatArray(16) { 22f }, FloatArray(16) { 41f }, FloatArray(16) { 255f },
        )
        return nowcast to listOf(weather)
    }

    private fun save(b: Bitmap, name: String) = File(dir, name).outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }

    @Test fun widget() {
        assumeTrue(System.getProperty("shots") == "1")
        val (radar, weather) = scenario()
        val f = Forecaster.here(place, radar, weather, now)
        save(WidgetPainter.render(Snapshot.of(f, "Romainville"), 1000, 420, 2.75f, now + 60), "widget_rain.png")
        val dry = Forecaster.here(place, null, listOf(PointWeather(place, weather[0].models.map { ModelSeries(it.id, it.label, it.weight, it.endS, FloatArray(16)) }, weather[0].timeS, weather[0].tempC, weather[0].apparentC, weather[0].windKmh, weather[0].gustKmh, weather[0].windDirDeg)), now)
        save(WidgetPainter.render(Snapshot.of(dry, "Romainville"), 1000, 420, 2.75f, now + 60), "widget_dry.png")
        save(WidgetPainter.render(null, 1000, 300, 2.75f, now), "widget_empty.png")
    }

    @Test fun here() {
        assumeTrue(System.getProperty("shots") == "1")
        val (radar, weather) = scenario()
        val f = Forecaster.here(place, radar, weather, now)
        captureRoboImage(File(dir, "screen_here.png").path) {
            AppTheme { Surface(Modifier.fillMaxSize()) { HereScreen(HereState.Ready(Place("Romainville", place), f), false, {}, {}) } }
        }
    }

    @Test fun trip() {
        assumeTrue(System.getProperty("shots") == "1")
        val (radar, weather) = scenario()
        val to = Place("Place de la République 75011 Paris", LatLon(48.8675, 2.3638))
        val route = Route(listOf(place, LatLon(48.878, 2.40), to.point))
        val f = Forecaster.trip(route, 18.0, radar, weather, now)
        captureRoboImage(File(dir, "screen_trip.png").path) {
            AppTheme {
                Surface(Modifier.fillMaxSize()) {
                    TripScreen(
                        null, to, TripState.Ready("Ma position", to.label, f), 0, listOf(SavedTrip("Maison → Bureau", null, to, null)), 18,
                        {}, {}, {}, {}, {}, {}, {}, {}, { emptyList() },
                    )
                }
            }
        }
    }

    @Test fun settings() {
        assumeTrue(System.getProperty("shots") == "1")
        captureRoboImage(File(dir, "screen_settings.png").path) {
            AppTheme { Surface(Modifier.fillMaxSize()) { SettingsScreen(18, true, true, false, {}, {}, {}, {}) } }
        }
    }
}
