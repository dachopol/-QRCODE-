package com.aistudio.qrgenerator.kmpzqr.util

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

    private fun enabledProviders(manager: LocationManager): List<String> =
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { provider ->
                runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)
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
        val providers = enabledProviders(manager)

        if (providers.isEmpty()) {
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
                val signals = providers.associateWith { CancellationSignal() }
                var remaining = providers.size

                val timeout = Runnable {
                    if (!completed) {
                        signals.values.forEach { signal -> runCatching { signal.cancel() } }
                        finish(null)
                    }
                }
                handler.postDelayed(timeout, REQUEST_TIMEOUT_MS)

                providers.forEach { provider ->
                    manager.getCurrentLocation(
                        provider,
                        signals.getValue(provider),
                        ContextCompat.getMainExecutor(context)
                    ) { location ->
                        if (completed) return@getCurrentLocation

                        if (isUsable(location)) {
                            handler.removeCallbacks(timeout)
                            signals.values.forEach { signal -> runCatching { signal.cancel() } }
                            finish(location)
                        } else {
                            remaining -= 1
                            if (remaining <= 0) {
                                handler.removeCallbacks(timeout)
                                finish(null)
                            }
                        }
                    }
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
                        if (!isUsable(location) || completed) return
                        handler.removeCallbacks(timeout)
                        runCatching { manager.removeUpdates(this) }
                        finish(location)
                    }

                    @Deprecated("Deprecated in Android")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                    override fun onProviderEnabled(provider: String) = Unit

                    override fun onProviderDisabled(provider: String) = Unit
                }

                handler.postDelayed(timeout, REQUEST_TIMEOUT_MS)
                providers.forEach { provider ->
                    @Suppress("DEPRECATION")
                    manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                }
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
