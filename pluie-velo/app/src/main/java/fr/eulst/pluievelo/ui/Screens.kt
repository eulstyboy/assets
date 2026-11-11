package fr.eulst.pluievelo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.eulst.pluievelo.core.Advice
import fr.eulst.pluievelo.core.Confidence
import fr.eulst.pluievelo.core.Place
import fr.eulst.pluievelo.core.TripOption
import fr.eulst.pluievelo.core.WET_RATE
import fr.eulst.pluievelo.core.round1
import fr.eulst.pluievelo.data.SavedTrip
import fr.eulst.pluievelo.widget.ChartPainter
import fr.eulst.pluievelo.widget.Fmt
import fr.eulst.pluievelo.widget.Palette
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

val Muted = Color(Palette.MUTED)
val Accent = Color(Palette.ACCENT)
val CardColor = Color(0xFF16284B)

fun mmText(mm: Float): String = if (mm < 0.05f) "sec" else "${mm.round1()} mm"

@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(20.dp),
    ) { Column(Modifier.padding(16.dp)) { content() } }
}

@Composable
fun RainChart(rates: FloatArray, probs: FloatArray?, labels: List<ChartPainter.Label>, modifier: Modifier = Modifier) {
    val density = LocalDensity.current.density
    Canvas(modifier) {
        drawIntoCanvas { ChartPainter.draw(it.nativeCanvas, 0f, 0f, size.width, size.height, rates, probs, labels, density, true) }
    }
}

@Composable
fun AdviceBlock(heading: String, a: Advice) {
    Panel {
        Text(heading, color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(Color(Palette.level(a.level))))
            Spacer(Modifier.width(8.dp))
            Text(a.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        a.items.forEach { Text("•  $it", fontSize = 15.sp, modifier = Modifier.padding(vertical = 1.dp)) }
        a.notes.forEach {
            Spacer(Modifier.height(6.dp))
            Text(it, color = Color(0xFFFFCB4D), fontSize = 14.sp)
        }
    }
}

@Composable
fun ConfidenceLine(c: Confidence, text: String) {
    val color = when (c) {
        Confidence.HIGH -> Color(0xFF4CD08A)
        Confidence.MEDIUM -> Color(0xFFFFCB4D)
        Confidence.LOW -> Color(0xFFFF9840)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text("Confiance ${c.label}. $text", color = Muted, fontSize = 13.sp)
    }
}

@Composable
fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, color = Muted, fontSize = 12.sp)
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

// -------------------------------------------------------------------------------------------------

@Composable
fun HereScreen(state: HereState, refreshing: Boolean, onRefresh: () -> Unit, onGrant: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (state) {
            HereState.Loading -> Box(Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            HereState.NeedsLocation -> Panel {
                Text("Où êtes-vous ?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Pluie Vélo a besoin de votre position pour prévoir la pluie là où vous êtes. Elle n'est envoyée qu'aux services météo, sans aucun compte.", color = Muted)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onGrant) { Text("Autoriser la localisation") }
            }

            is HereState.Failed -> Panel {
                Text("Prévision indisponible", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(state.message, color = Muted)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRefresh) { Text("Réessayer") }
            }

            is HereState.Ready -> {
                val f = state.forecast
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Muted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        state.place.label.ifBlank { "Ma position" } + "  ·  " + Fmt.time(f.nowS),
                        color = Muted, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    if (refreshing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    else IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "Actualiser", tint = Accent) }
                }
                Column {
                    Text(f.headline.title, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
                    Text(f.headline.subtitle, fontSize = 17.sp, color = Muted)
                }
                Panel {
                    val n = f.slots.size
                    RainChart(
                        FloatArray(n) { f.slots[it].rate }, FloatArray(n) { f.slots[it].prob },
                        (0 until n step 6).map { ChartPainter.Label(it.toFloat() / n, if (it == 0) "Maintenant" else Fmt.time(f.slots[it].startS)) },
                        Modifier.fillMaxWidth().height(170.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Row {
                        Stat("Cumul sur 1 h", mmText(f.mmNextHour), Modifier.weight(1f))
                        Stat("Intensité max", if (f.maxRate < WET_RATE) "—" else "${f.maxRate.round1()} mm/h", Modifier.weight(1f))
                        Stat("Température", f.tempC?.let { "${it.roundToInt()} °C" } ?: "—", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    ConfidenceLine(f.confidence, f.confidenceText)
                    f.motionText?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = Muted, fontSize = 13.sp)
                    }
                }
                AdviceBlock("Pour une sortie de 30 min maintenant", f.advice)
                Panel {
                    Text("Ce que dit chaque source pour l'heure qui vient", color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    f.sources.forEach { s ->
                        Row(Modifier.padding(vertical = 3.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(s.label, fontSize = 15.sp)
                                if (s.detail != "modèle") Text(s.detail, color = Muted, fontSize = 12.sp)
                            }
                            Text(s.mm?.let(::mmText) ?: "—", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------

@Composable
fun AddressField(
    label: String, place: Place?, hereAllowed: Boolean, onPlace: (Place?) -> Unit, search: suspend (String) -> List<Place>,
) {
    var text by remember { mutableStateOf(place?.label ?: "") }
    var edited by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Place>>(emptyList()) }
    // Follow the chosen place, but leave the text alone while the user is typing a new one.
    LaunchedEffect(place) {
        if (place != null) text = place.label else if (!edited) text = ""
        edited = false
    }
    LaunchedEffect(text) {
        if (text.length < 3 || text == place?.label) {
            results = emptyList()
        } else {
            delay(350)
            results = search(text)
        }
    }
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                if (place != null && it != place.label) {
                    edited = true
                    onPlace(null)
                }
            },
            label = { Text(if (hereAllowed && text.isEmpty()) "$label : ma position" else label) },
            placeholder = { Text(if (hereAllowed) "Ma position" else "Adresse ou lieu") },
            singleLine = true,
            trailingIcon = {
                if (text.isNotEmpty()) IconButton(onClick = { text = ""; onPlace(null) }) { Icon(Icons.Default.Close, "Effacer") }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        results.forEach { p ->
            Text(
                p.label, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().clickable { results = emptyList(); onPlace(p) }.padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
fun OptionChip(o: TripOption, index: Int, selected: Boolean, best: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF244070) else CardColor),
        border = if (selected) BorderStroke(1.5.dp, Accent) else null,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp).width(62.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (index == 0) "Maint." else Fmt.time(o.departS), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Box(Modifier.size(10.dp).clip(CircleShape).background(Color(Palette.level(o.advice.level))))
            Spacer(Modifier.height(6.dp))
            Text(mmText(o.mm), fontSize = 13.sp, color = Muted)
            Text(if (best) "Conseillé" else "", fontSize = 11.sp, color = Accent)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripScreen(
    from: Place?, to: Place?, state: TripState, selected: Int, trips: List<SavedTrip>, speed: Int,
    onFrom: (Place?) -> Unit, onTo: (Place?) -> Unit, onSwap: () -> Unit, onCompute: () -> Unit, onSelect: (Int) -> Unit,
    onSave: () -> Unit, onLoad: (SavedTrip) -> Unit, onDelete: (SavedTrip) -> Unit, search: suspend (String) -> List<Place>,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (trips.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                trips.forEach { t ->
                    Row(
                        Modifier.clip(RoundedCornerShape(50)).background(CardColor).clickable { onLoad(t) }.padding(start = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(t.name, fontSize = 14.sp, maxLines = 1)
                        IconButton(onClick = { onDelete(t) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Close, "Supprimer", tint = Muted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
        Panel {
            AddressField("Départ", from, true, onFrom, search)
            Spacer(Modifier.height(8.dp))
            AddressField("Arrivée", to, false, onTo, search)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onCompute, enabled = to != null && state != TripState.Loading) { Text("Pluie sur le trajet") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onSwap, enabled = to != null) { Text("Inverser") }
                Spacer(Modifier.weight(1f))
                Text("$speed km/h", color = Muted, fontSize = 13.sp)
            }
        }
        when (state) {
            TripState.Idle -> Text("Indiquez une arrivée : l'appli calcule la pluie que vous recevrez sur l'itinéraire vélo, minute par minute, et le meilleur moment pour partir.", color = Muted, fontSize = 14.sp)
            TripState.Loading -> Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is TripState.Failed -> Panel { Text(state.message) }
            is TripState.Ready -> {
                val f = state.forecast
                val o = f.options[selected.coerceIn(0, f.options.lastIndex)]
                val best = f.options[f.bestIndex]
                Column {
                    val title = when {
                        o.mm < 0.05f -> "Trajet au sec"
                        else -> "${o.mm.round1()} mm de pluie sur le trajet"
                    }
                    Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
                    val detail = if (o.mm < 0.05f) "" else " · ${o.wetMin} min sous la pluie, jusqu'à ${o.maxRate.round1()} mm/h"
                    Text("Départ ${Fmt.time(o.departS)}, arrivée ${Fmt.time(o.departS + f.durationMin * 60L)}$detail", color = Muted, fontSize = 15.sp)
                    Text("%.1f km · %d min à %d km/h".format(f.distanceKm, f.durationMin, speed).replace('.', ','), color = Muted, fontSize = 13.sp)
                }
                if (f.bestIndex != 0 && selected != f.bestIndex) {
                    Panel {
                        Text("Partez plutôt à ${Fmt.time(best.departS)} : ${mmText(best.mm)} au lieu de ${mmText(f.options[0].mm)}.", fontSize = 15.sp)
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    f.options.forEachIndexed { i, opt -> OptionChip(opt, i, i == selected, i == f.bestIndex && f.options[0].mm >= 0.1f) { onSelect(i) } }
                }
                Panel {
                    Text("Pluie le long du trajet", color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    RainChart(
                        o.minutes, null,
                        listOf(
                            ChartPainter.Label(0f, "Départ " + Fmt.time(o.departS)),
                            ChartPainter.Label(0.5f, "mi-parcours"),
                            ChartPainter.Label(1f, "Arrivée " + Fmt.time(o.departS + f.durationMin * 60L)),
                        ),
                        Modifier.fillMaxWidth().height(150.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    ConfidenceLine(f.confidence, f.confidenceText)
                    f.windText?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = Muted, fontSize = 13.sp)
                    }
                }
                AdviceBlock("Équipement pour ce départ", o.advice)
                OutlinedButton(onClick = onSave) { Text("Enregistrer ce trajet") }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------

@Composable
fun SettingsScreen(
    speed: Int, alerts: Boolean, notificationsGranted: Boolean, backgroundGranted: Boolean,
    onSpeed: (Int) -> Unit, onAlerts: (Boolean) -> Unit, onBackground: () -> Unit, onBattery: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Panel {
            Text("Vitesse moyenne à vélo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("$speed km/h", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Slider(value = speed.toFloat(), onValueChange = { onSpeed(it.roundToInt()) }, valueRange = 10f..32f, steps = 21)
            Text("Sert à savoir où vous serez à chaque minute du trajet.", color = Muted, fontSize = 13.sp)
        }
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Alerte quand la pluie arrive", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Notification si la pluie commence dans la demi-heure là où vous êtes. Vérification toutes les 15 minutes environ.", color = Muted, fontSize = 13.sp)
                }
                Switch(checked = alerts, onCheckedChange = onAlerts)
            }
            if (alerts && !notificationsGranted) {
                Spacer(Modifier.height(8.dp))
                Text("Les notifications ne sont pas autorisées pour l'appli.", color = Color(0xFFFFCB4D), fontSize = 13.sp)
            }
        }
        Panel {
            Text("Suivre ma position en arrière-plan", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(
                if (backgroundGranted) "Autorisé : le widget et les alertes suivent vos déplacements."
                else "Sans l'autorisation « Toujours », le widget et les alertes utilisent la dernière position connue à l'ouverture de l'appli.",
                color = Muted, fontSize = 13.sp,
            )
            if (!backgroundGranted) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onBackground) { Text("Autoriser « Toujours »") }
            }
            Spacer(Modifier.height(8.dp))
            Text("Si les mises à jour s'espacent, excluez l'appli de l'économie de batterie.", color = Muted, fontSize = 13.sp)
            TextButton(onClick = onBattery) { Text("Réglages de batterie") }
        }
        Panel {
            Text("Sources", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Radar : RainViewer (mosaïque des radars nationaux, une image toutes les 10 min), extrapolé par l'appli selon le déplacement mesuré des averses.\n" +
                    "Modèles : Météo-France AROME, DWD ICON, KNMI, UK Met Office et ECMWF via Open-Meteo (CC BY 4.0).\n" +
                    "Itinéraires : © contributeurs OpenStreetMap, serveur routing.openstreetmap.de.\n" +
                    "Adresses : Géoplateforme IGN / Base Adresse Nationale.",
                color = Muted, fontSize = 13.sp, lineHeight = 19.sp,
            )
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val scheme = androidx.compose.material3.darkColorScheme(
        primary = Accent, onPrimary = Color(0xFF06223F), background = Color(0xFF0E1A33), surface = Color(0xFF0E1A33),
        onBackground = Color(Palette.TEXT), onSurface = Color(Palette.TEXT), surfaceVariant = CardColor,
        onSurfaceVariant = Muted, secondaryContainer = Color(0xFF244070), onSecondaryContainer = Color(Palette.TEXT),
        outline = Color(0xFF3A4E78),
    )
    MaterialTheme(colorScheme = scheme, content = content)
}
