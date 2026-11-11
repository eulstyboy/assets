package fr.eulst.pluievelo.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import fr.eulst.pluievelo.widget.Alerts
import fr.eulst.pluievelo.widget.RefreshWorker

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    private fun granted(p: String) = ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Alerts.ensureChannel(this)
        RefreshWorker.schedule(this)

        setContent {
            AppTheme {
                var tab by rememberSaveable { mutableIntStateOf(0) }
                var notificationsOk by rememberSaveable { mutableStateOf(Build.VERSION.SDK_INT < 33 || granted(Manifest.permission.POST_NOTIFICATIONS)) }
                var backgroundOk by rememberSaveable { mutableStateOf(vm.repo.hasBackgroundLocation()) }

                val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notificationsOk = it }
                val location = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                    vm.refreshHere()
                    if (Build.VERSION.SDK_INT >= 33 && !notificationsOk) notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                val background = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { backgroundOk = it }
                fun askLocation() = location.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))

                LaunchedEffect(Unit) { if (!vm.repo.hasLocationPermission()) askLocation() }
                // Refresh every time the app comes back to the front.
                LifecycleResumeEffect(Unit) {
                    backgroundOk = vm.repo.hasBackgroundLocation()
                    notificationsOk = Build.VERSION.SDK_INT < 33 || granted(Manifest.permission.POST_NOTIFICATIONS)
                    vm.refreshHere()
                    onPauseOrDispose { }
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(tab == 0, { tab = 0 }, { Icon(Icons.Default.LocationOn, null) }, label = { Text("Ici") })
                            NavigationBarItem(tab == 1, { tab = 1 }, { Icon(Icons.Default.Place, null) }, label = { Text("Trajet") })
                            NavigationBarItem(tab == 2, { tab = 2 }, { Icon(Icons.Default.Settings, null) }, label = { Text("Réglages") })
                        }
                    },
                ) { inner ->
                    Box(Modifier.fillMaxSize().padding(inner)) {
                        when (tab) {
                            0 -> HereScreen(vm.here, vm.refreshing, vm::refreshHere, ::askLocation)
                            1 -> TripScreen(
                                vm.from, vm.to, vm.trip, vm.selected, vm.trips, vm.speed,
                                onFrom = { vm.from = it }, onTo = { vm.to = it }, onSwap = vm::swap, onCompute = vm::computeTrip,
                                onSelect = { vm.selected = it }, onSave = vm::saveTrip, onLoad = vm::loadTrip, onDelete = vm::deleteTrip,
                                search = vm::search,
                            )
                            else -> SettingsScreen(
                                vm.speed, vm.alerts, notificationsOk, backgroundOk,
                                onSpeed = vm::updateSpeed,
                                onAlerts = {
                                    vm.updateAlerts(it)
                                    if (it && Build.VERSION.SDK_INT >= 33 && !notificationsOk) notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                onBackground = {
                                    if (!vm.repo.hasLocationPermission()) askLocation()
                                    else background.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                },
                                onBattery = {
                                    runCatching { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
