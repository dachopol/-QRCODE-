package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

enum class LocationError {
    PERMISSION_REQUIRED,
    SERVICES_DISABLED,
    UNAVAILABLE
}

object CurrentLocationProvider {
    private const val LAST_KNOWN_MAX_AGE_MS = 120_000L
    private const val REQUEST_TIMEOUT_MS = 15_000L

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    private fun isUsable(location: Location?): Boolean {
        if (location == null) return false
        val latitude = location.latitude
        val longitude = location.longitude
        return latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0 &&
            !(latitude == 0.0 && longitude == 0.0)
    }

    @SuppressLint("MissingPermission")
    private fun recentLastKnown(manager: LocationManager): Location? {
        val now = System.currentTimeMillis()
        return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .asSequence()
            .mapNotNull { provider ->
                runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            }
            .filter(::isUsable)
            .filter { location ->
                val age = now - location.time
                location.time > 0L && age in 0..LAST_KNOWN_MAX_AGE_MS
            }
            .maxByOrNull { it.time }
    }

    @SuppressLint("MissingPermission")
    fun requestCurrentLocation(
        context: Context,
        onResult: (Location?) -> Unit,
        onError: (LocationError) -> Unit
    ) {
        if (!hasPermission(context)) {
            onError(LocationError.PERMISSION_REQUIRED)
            return
        }

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled =
            runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)
        val networkEnabled =
            runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)

        val provider = when {
            gpsEnabled -> LocationManager.GPS_PROVIDER
            networkEnabled -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (provider == null) {
            onError(LocationError.SERVICES_DISABLED)
            return
        }

        val fallback = recentLastKnown(manager)
        val handler = Handler(Looper.getMainLooper())
        var completed = false

        fun finish(location: Location?, unavailableError: LocationError = LocationError.UNAVAILABLE) {
            if (completed) return
            completed = true
            val selected = location?.takeIf(::isUsable) ?: fallback
            if (selected != null) onResult(selected) else onError(unavailableError)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val cancellationSignal = CancellationSignal()
                val timeout = Runnable {
                    if (!completed) {
                        cancellationSignal.cancel()
                        finish(null)
                    }
                }
                handler.postDelayed(timeout, REQUEST_TIMEOUT_MS)

                manager.getCurrentLocation(
                    provider,
                    cancellationSignal,
                    ContextCompat.getMainExecutor(context)
                ) { location ->
                    handler.removeCallbacks(timeout)
                    finish(location)
                }
            } else {
                lateinit var listener: LocationListener
                val timeout = Runnable {
                    if (!completed) {
                        runCatching { manager.removeUpdates(listener) }
                        finish(null)
                    }
                }

                listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        handler.removeCallbacks(timeout)
                        runCatching { manager.removeUpdates(this) }
                        finish(location)
                    }

                    @Deprecated("Deprecated in Android")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                    override fun onProviderEnabled(provider: String) = Unit

                    override fun onProviderDisabled(provider: String) {
                        handler.removeCallbacks(timeout)
                        runCatching { manager.removeUpdates(this) }
                        finish(null, LocationError.SERVICES_DISABLED)
                    }
                }

                handler.postDelayed(timeout, REQUEST_TIMEOUT_MS)
                @Suppress("DEPRECATION")
                manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            }
        } catch (_: SecurityException) {
            if (!completed) {
                completed = true
                onError(LocationError.PERMISSION_REQUIRED)
            }
        } catch (_: Exception) {
            finish(null)
        }
    }
}
