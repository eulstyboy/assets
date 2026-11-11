package fr.eulst.pluievelo.widget

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.eulst.pluievelo.R
import fr.eulst.pluievelo.core.HereForecast
import fr.eulst.pluievelo.core.Place
import fr.eulst.pluievelo.core.round1
import fr.eulst.pluievelo.data.Repo
import fr.eulst.pluievelo.ui.MainActivity
import java.util.concurrent.TimeUnit

class RainWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { draw(context, manager, it) }
        RefreshWorker.schedule(context)
        RefreshWorker.now(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle) {
        draw(context, manager, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) RefreshWorker.now(context)
    }

    companion object {
        const val ACTION_REFRESH = "fr.eulst.pluievelo.REFRESH"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            manager.getAppWidgetIds(ComponentName(context, RainWidget::class.java)).forEach { draw(context, manager, it) }
        }

        private fun draw(context: Context, manager: AppWidgetManager, id: Int) {
            val dm = context.resources.displayMetrics
            val opts = manager.getAppWidgetOptions(id)
            val portrait = context.resources.configuration.orientation != android.content.res.Configuration.ORIENTATION_LANDSCAPE
            val wDp = opts.getInt(if (portrait) AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH else AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 320).coerceIn(180, 520)
            val hDp = opts.getInt(if (portrait) AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 150).coerceIn(90, 320)
            // Cap the bitmap size: RemoteViews has a memory budget.
            val density = minOf(dm.density, 900f / wDp)
            val bmp = WidgetPainter.render(
                Repo.get(context).store.snapshot, (wDp * density).toInt(), (hDp * density).toInt(), density, System.currentTimeMillis() / 1000,
            )
            val views = RemoteViews(context.packageName, R.layout.widget)
            views.setImageViewBitmap(R.id.image, bmp)
            views.setOnClickPendingIntent(
                R.id.image,
                PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT),
            )
            views.setOnClickPendingIntent(
                R.id.refresh,
                PendingIntent.getBroadcast(
                    context, 1, Intent(context, RainWidget::class.java).setAction(ACTION_REFRESH),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
            manager.updateAppWidget(id, views)
        }
    }
}

/** Refreshes the forecast in the background, redraws the widget and raises the rain alert. */
class RefreshWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result = try {
        val repo = Repo.get(applicationContext)
        val (place, forecast) = repo.refreshHere(foreground = false)
        Alerts.maybeNotify(applicationContext, place, forecast)
        Result.success()
    } catch (e: Exception) {
        RainWidget.updateAll(applicationContext)
        if (runAttemptCount < 2) Result.retry() else Result.success()
    }

    companion object {
        private val online = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        /** Every 15 minutes: the shortest period Android allows for background work. */
        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "refresh", ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<RefreshWorker>(15, TimeUnit.MINUTES).setConstraints(online).build(),
            )
        }

        fun now(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "refresh-now", ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<RefreshWorker>().setConstraints(online).build(),
            )
        }
    }
}

object Alerts {
    private const val CHANNEL = "pluie"
    private const val COOLDOWN_S = 90 * 60L

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Alertes pluie", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Prévient quand la pluie arrive là où vous êtes"
            },
        )
    }

    /** Alert when rain starts within 30 minutes, at most once per shower. */
    fun maybeNotify(context: Context, place: Place, f: HereForecast) {
        val store = Repo.get(context).store
        val start = f.headline.startInMin ?: return
        if (!store.alerts || f.headline.raining || start !in 1..30) return
        if (f.nowS - store.lastAlertS < COOLDOWN_S) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        ensureChannel(context)
        val where = if (place.label.isBlank()) "" else " à ${place.label}"
        val text = f.headline.subtitle.replaceFirstChar { it.uppercase() } +
            " · ${f.mmNextHour.round1()} mm sur l'heure · ${f.advice.items.take(2).joinToString(", ")}"
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_rain)
            .setContentTitle("${f.headline.title}$where")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setTimeoutAfter(60 * 60 * 1000L)
            .setContentIntent(
                PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT),
            )
            .build()
        context.getSystemService(NotificationManager::class.java).notify(1, n)
        store.lastAlertS = f.nowS
    }
}
