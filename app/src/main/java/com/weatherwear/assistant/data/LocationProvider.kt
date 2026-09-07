package com.weatherwear.assistant.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Определение координат штатным [LocationManager] — без Google Play Services,
 * чтобы APK ставился на любой Android-телефон.
 */
class LocationProvider(private val context: Context) {

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** Возвращает место по координатам телефона либо null, если получить их не удалось. */
    suspend fun currentPlace(): Place? {
        if (!hasPermission()) return null
        val location = lastKnownLocation() ?: requestFreshLocation() ?: return null
        val name = reverseGeocode(location) ?: "Моё местоположение"
        return Place(
            name = name,
            latitude = location.latitude,
            longitude = location.longitude,
        )
    }

    private fun manager(): LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private fun lastKnownLocation(): Location? {
        val manager = manager() ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
        return providers.mapNotNull { provider ->
            runCatching {
                if (manager.isProviderEnabled(provider)) manager.getLastKnownLocation(provider) else null
            }.getOrNull()
        }.filter {
            // Координаты старше суток обычно уже не про текущий город.
            System.currentTimeMillis() - it.time < 24 * 60 * 60 * 1000L
        }.maxByOrNull { it.time }
    }

    private suspend fun requestFreshLocation(): Location? = withTimeoutOrNull(20_000L) {
        val manager = manager() ?: return@withTimeoutOrNull null
        val provider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .firstOrNull { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
            ?: return@withTimeoutOrNull null

        suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    runCatching { manager.removeUpdates(this) }
                    if (continuation.isActive) continuation.resume(location)
                }

                @Deprecated("Требуется для совместимости со старыми версиями Android")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit

                override fun onProviderDisabled(provider: String) = Unit

                override fun onProviderEnabled(provider: String) = Unit
            }

            try {
                manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            } catch (e: SecurityException) {
                if (continuation.isActive) continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            continuation.invokeOnCancellation {
                runCatching { manager.removeUpdates(listener) }
            }
        }
    }

    private suspend fun reverseGeocode(location: Location): String? = withContext(Dispatchers.IO) {
        runCatching {
            @Suppress("DEPRECATION")
            val addresses = Geocoder(context, Locale("ru"))
                .getFromLocation(location.latitude, location.longitude, 1)
            val address = addresses?.firstOrNull()
            address?.locality
                ?: address?.subAdminArea
                ?: address?.adminArea
        }.getOrNull()
    }
}
