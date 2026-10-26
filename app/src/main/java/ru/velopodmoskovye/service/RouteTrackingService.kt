package ru.velopodmoskovye.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.velopodmoskovye.MainActivity
import ru.velopodmoskovye.R
import ru.velopodmoskovye.data.content.ContentRepository
import ru.velopodmoskovye.data.location.LocationTracker
import ru.velopodmoskovye.data.progress.ProgressRepository
import ru.velopodmoskovye.data.settings.SettingsRepository
import ru.velopodmoskovye.domain.geo.Geo
import ru.velopodmoskovye.domain.model.Destination
import ru.velopodmoskovye.domain.progress.EarnedTitle
import ru.velopodmoskovye.domain.progress.Fix
import ru.velopodmoskovye.domain.progress.Geofence
import ru.velopodmoskovye.ui.Format
import javax.inject.Inject

/**
 * Фоновый трекинг — только пока идёт маршрут. Раз в 10–20 с проверяет геофенсы
 * точек активного места, ставит отметки и показывает в уведомлении следующую точку.
 */
@AndroidEntryPoint
class RouteTrackingService : LifecycleService() {
    @Inject lateinit var tracker: LocationTracker
    @Inject lateinit var progress: ProgressRepository
    @Inject lateinit var content: ContentRepository
    @Inject lateinit var settings: SettingsRepository

    private var job: Job? = null
    private var previous: Fix? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_STOP) {
            stopTracking()
            return START_NOT_STICKY
        }
        ensureChannel(this)
        val notification = buildNotification(getString(R.string.tracking_title), getString(R.string.tracking_waiting))
        ServiceCompat.startForeground(
            this, NOTIFICATION_ID, notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0,
        )
        // Новый старт (в том числе другого места) перезапускает слежение с актуальным маршрутом.
        job?.cancel()
        previous = null
        job = lifecycleScope.launch { track() }
        return START_STICKY
    }

    private suspend fun track() {
        val active = progress.activeRouteOnce()
        if (active == null || !tracker.hasPermission()) {
            stopTracking()
            return
        }
        val dest = content.pack().destinations[active.destinationId] ?: run { stopTracking(); return }
        tracker.updates().collect { fix -> onFix(dest, fix) }
    }

    private suspend fun onFix(dest: Destination, fix: Fix) {
        // Километраж: только по точным отметкам и без «телепортов».
        previous?.let { prev ->
            val d = Geo.distanceM(prev.point, fix.point)
            if (fix.accuracyM <= 50 && prev.accuracyM <= 50 && d in 10.0..2_000.0) progress.addMeters(d)
        }
        if (fix.accuracyM <= 50) previous = fix

        val radius = settings.settings.first().geofenceRadiusM
        val visited = progress.visitedIds.first()
        val fresh = mutableListOf<EarnedTitle>()
        for (stop in Geofence.hit(dest.stops, visited, fix, radius)) {
            fresh += progress.markVisited(stop.id, dest.id, fix, "auto", Geo.distanceM(stop.coords, fix.point))
            notifyEvent(getString(R.string.stop_reached, stop.name), stop.name)
        }
        fresh.forEach { notifyEvent(getString(R.string.title_awarded), it.name) }

        val nowVisited = progress.visitedIds.first()
        val next = dest.stops.firstOrNull { it.id !in nowVisited }
        if (next == null) {
            updateNotification(dest.name, getString(R.string.all_stops_done))
            progress.finishRoute()
            stopTracking()
            return
        }
        val km = Geo.distanceKm(fix.point, next.coords)
        updateNotification(dest.name, getString(R.string.next_stop, next.name, Format.km(km)))
    }

    private fun stopTracking() {
        job?.cancel()
        job = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        this, 0, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun buildNotification(title: String, text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ROUTE)
            .setSmallIcon(R.drawable.ic_stat_bike)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppIntent())
            .addAction(0, getString(R.string.stop_tracking), PendingIntent.getService(
                this, 1, Intent(this, RouteTrackingService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE,
            ))
            .build()

    private fun updateNotification(title: String, text: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification(title, text))
    }

    private fun notifyEvent(title: String, text: String) {
        val n = NotificationCompat.Builder(this, CHANNEL_EVENTS)
            .setSmallIcon(R.drawable.ic_stat_bike)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()
        getSystemService(NotificationManager::class.java).notify(text.hashCode(), n)
    }

    companion object {
        const val ACTION_STOP = "ru.velopodmoskovye.STOP_TRACKING"
        private const val NOTIFICATION_ID = 42
        private const val CHANNEL_ROUTE = "route"
        private const val CHANNEL_EVENTS = "events"

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, RouteTrackingService::class.java))
        }

        fun stop(context: Context) {
            context.startService(Intent(context, RouteTrackingService::class.java).setAction(ACTION_STOP))
        }

        fun ensureChannel(context: Context) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(CHANNEL_ROUTE, context.getString(R.string.channel_route), NotificationManager.IMPORTANCE_LOW))
            nm.createNotificationChannel(NotificationChannel(CHANNEL_EVENTS, context.getString(R.string.channel_events), NotificationManager.IMPORTANCE_DEFAULT))
        }
    }
}
