package ru.velopodmoskovye.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.domain.progress.Fix
import javax.inject.Inject
import javax.inject.Singleton

/** Геолокация через FusedLocationProvider. Последняя точка доступна всем экранам. */
@Singleton
class LocationTracker @Inject constructor(@ApplicationContext private val context: Context) {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }
    private val _last = MutableStateFlow<Fix?>(null)
    val last: StateFlow<Fix?> = _last.asStateFlow()

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** Поток координат. Батарею бережём: обновление раз в [intervalMs] (по умолчанию 15 с). */
    @SuppressLint("MissingPermission")
    fun updates(intervalMs: Long = DEFAULT_INTERVAL_MS): Flow<Fix> = callbackFlow {
        if (!hasPermission()) {
            close()
            return@callbackFlow
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(MIN_INTERVAL_MS)
            .setMaxUpdateDelayMillis(MAX_INTERVAL_MS)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    val fix = loc.toFix()
                    _last.value = fix
                    trySend(fix)
                }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }

    @SuppressLint("MissingPermission")
    suspend fun current(): Fix? {
        if (!hasPermission()) return null
        return runCatching {
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()?.toFix()
        }.getOrNull()?.also { _last.value = it }
    }

    private fun Location.toFix() = Fix(GeoPoint(latitude, longitude), accuracy.toDouble(), time)

    companion object {
        const val DEFAULT_INTERVAL_MS = 15_000L
        const val MIN_INTERVAL_MS = 10_000L
        const val MAX_INTERVAL_MS = 20_000L
    }
}
