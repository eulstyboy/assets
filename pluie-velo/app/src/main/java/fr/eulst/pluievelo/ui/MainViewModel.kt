package fr.eulst.pluievelo.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.eulst.pluievelo.core.Geo
import fr.eulst.pluievelo.core.HereForecast
import fr.eulst.pluievelo.core.LatLon
import fr.eulst.pluievelo.core.Place
import fr.eulst.pluievelo.core.Route
import fr.eulst.pluievelo.core.TripForecast
import fr.eulst.pluievelo.data.NoLocationException
import fr.eulst.pluievelo.data.Repo
import fr.eulst.pluievelo.data.SavedTrip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface HereState {
    data object Loading : HereState
    data object NeedsLocation : HereState
    data class Failed(val message: String) : HereState
    data class Ready(val place: Place, val forecast: HereForecast) : HereState
}

sealed interface TripState {
    data object Idle : TripState
    data object Loading : TripState
    data class Failed(val message: String) : TripState
    data class Ready(val from: String, val to: String, val forecast: TripForecast) : TripState
}

class MainViewModel(app: Application) : AndroidViewModel(app) {
    val repo = Repo.get(app)

    var here by mutableStateOf<HereState>(HereState.Loading)
        private set
    var refreshing by mutableStateOf(false)
        private set

    /** Null means "my position". */
    var from by mutableStateOf<Place?>(null)
    var to by mutableStateOf<Place?>(null)
    var trip by mutableStateOf<TripState>(TripState.Idle)
        private set
    var selected by mutableIntStateOf(0)
    var trips by mutableStateOf(repo.store.trips)
        private set
    var speed by mutableIntStateOf(repo.store.speedKmh)
        private set
    var alerts by mutableStateOf(repo.store.alerts)
        private set

    private var lastRoute: Triple<LatLon, LatLon, Route>? = null

    private fun message(e: Exception): String = when (e) {
        is NoLocationException -> "Position indisponible. Activez la localisation du téléphone."
        is java.io.IOException -> "Connexion impossible aux services météo. Vérifiez le réseau et réessayez."
        else -> e.message ?: "Erreur inattendue"
    }

    fun refreshHere() {
        if (refreshing) return
        if (!repo.hasLocationPermission() && repo.store.lastPlace == null) {
            here = HereState.NeedsLocation
            return
        }
        refreshing = true
        viewModelScope.launch {
            here = try {
                val (place, forecast) = withContext(Dispatchers.IO) { repo.refreshHere(foreground = true) }
                HereState.Ready(place, forecast)
            } catch (e: NoLocationException) {
                if (repo.hasLocationPermission()) HereState.Failed(message(e)) else HereState.NeedsLocation
            } catch (e: Exception) {
                (here as? HereState.Ready) ?: HereState.Failed(message(e))
            }
            refreshing = false
        }
    }

    suspend fun search(query: String): List<Place> = withContext(Dispatchers.IO) {
        runCatching { repo.routes.search(query, repo.store.lastPlace?.point) }.getOrDefault(emptyList())
    }

    fun computeTrip() {
        val dest = to ?: return
        val origin = from
        trip = TripState.Loading
        viewModelScope.launch {
            trip = try {
                val ready = withContext(Dispatchers.IO) {
                    val start = origin ?: repo.locate(foreground = true).let { Place("Ma position", it.point) }
                    val cached = lastRoute?.takeIf { Geo.distanceM(it.first, start.point) < 60 && it.second == dest.point }
                    val route = cached?.third ?: repo.routes.bikeRoute(start.point, dest.point).also {
                        lastRoute = Triple(start.point, dest.point, it)
                    }
                    val forecast = repo.engine.trip(route, speed.toDouble(), System.currentTimeMillis() / 1000)
                    TripState.Ready(start.label, dest.label, forecast)
                }
                selected = ready.forecast.bestIndex
                ready
            } catch (e: Exception) {
                TripState.Failed(if (e is IllegalStateException) "Aucun itinéraire vélo trouvé entre ces deux points." else message(e))
            }
        }
    }

    fun saveTrip() {
        val dest = to ?: return
        val name = (from?.label?.substringBefore(',')?.take(18) ?: "Ma position") + " → " + dest.label.substringBefore(',').take(22)
        trips = (trips.filter { it.name != name } + SavedTrip(name, from, dest, null)).takeLast(8)
        repo.store.trips = trips
    }

    fun deleteTrip(t: SavedTrip) {
        trips = trips - t
        repo.store.trips = trips
    }

    fun loadTrip(t: SavedTrip) {
        from = t.from
        to = t.to
        computeTrip()
    }

    fun swap() {
        val a = from
        val b = to
        if (a == null && b == null) return
        from = b
        to = a ?: repo.store.lastPlace?.let { Place("Ma position", it.point) }
        trip = TripState.Idle
    }

    fun updateSpeed(v: Int) {
        speed = v
        repo.store.speedKmh = v
    }

    fun updateAlerts(v: Boolean) {
        alerts = v
        repo.store.alerts = v
    }
}
