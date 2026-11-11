package fr.eulst.pluievelo.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Tasks
import fr.eulst.pluievelo.core.Engine
import fr.eulst.pluievelo.core.Geo
import fr.eulst.pluievelo.core.HereForecast
import fr.eulst.pluievelo.core.LatLon
import fr.eulst.pluievelo.core.Place
import fr.eulst.pluievelo.core.RouteSource
import fr.eulst.pluievelo.widget.RainWidget
import java.io.File
import java.util.concurrent.TimeUnit

class NoLocationException : Exception("Position indisponible")

/** Single entry point for the app, the widget and the background worker. All calls block. */
class Repo private constructor(private val context: Context) {
    val store = Store(context)
    private val fetcher = HttpFetcher(File(context.cacheDir, "radar"))
    val engine = Engine(fetcher, AndroidTileDecoder)
    val routes = RouteSource(fetcher)

    fun hasLocationPermission(): Boolean =
        granted(Manifest.permission.ACCESS_FINE_LOCATION) || granted(Manifest.permission.ACCESS_COARSE_LOCATION)

    fun hasBackgroundLocation(): Boolean =
        hasLocationPermission() && granted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    private fun granted(p: String) = ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED

    /**
     * Current position. In the background (without the "always" permission) the system refuses a
     * fresh fix, so the last known position is used instead.
     */
    @SuppressLint("MissingPermission")
    fun locate(foreground: Boolean): Place {
        val previous = store.lastPlace
        var point: LatLon? = null
        if (hasLocationPermission()) {
            val client = LocationServices.getFusedLocationProviderClient(context)
            if (foreground || hasBackgroundLocation()) {
                point = runCatching {
                    val cancel = CancellationTokenSource()
                    Tasks.await(client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancel.token), 12, TimeUnit.SECONDS)
                }.getOrNull()?.let { LatLon(it.latitude, it.longitude) }
            }
            if (point == null) {
                point = runCatching { Tasks.await(client.lastLocation, 5, TimeUnit.SECONDS) }.getOrNull()
                    ?.let { LatLon(it.latitude, it.longitude) }
            }
        }
        val p = point ?: return previous ?: throw NoLocationException()
        // Look the town up again only after moving a few hundred metres.
        val town = if (previous != null && previous.label.isNotBlank() && Geo.distanceM(previous.point, p) < 400) previous.label
        else routes.townName(p) ?: previous?.label?.takeIf { Geo.distanceM(previous.point, p) < 3000 } ?: ""
        return Place(town, p).also { store.lastPlace = it }
    }

    /** Refreshes the forecast for where the user is, and the widget with it. */
    fun refreshHere(foreground: Boolean): Pair<Place, HereForecast> {
        val place = locate(foreground)
        val forecast = engine.here(place.point, System.currentTimeMillis() / 1000)
        store.snapshot = Snapshot.of(forecast, place.label)
        RainWidget.updateAll(context)
        return place to forecast
    }

    companion object {
        @Volatile
        private var instance: Repo? = null

        fun get(context: Context): Repo = instance ?: synchronized(this) {
            instance ?: Repo(context.applicationContext).also { instance = it }
        }
    }
}
