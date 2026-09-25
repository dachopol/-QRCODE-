package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat

enum class LocationError {
    PERMISSION_REQUIRED,
    SERVICES_DISABLED,
    UNAVAILABLE
}

object CurrentLocationProvider {
    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

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
        val provider = when {
            runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) ->
                LocationManager.GPS_PROVIDER
            runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false) ->
                LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (provider == null) {
            onError(LocationError.SERVICES_DISABLED)
            return
        }

        val fallback = runCatching { manager.getLastKnownLocation(provider) }.getOrNull()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                manager.getCurrentLocation(
                    provider,
                    CancellationSignal(),
                    ContextCompat.getMainExecutor(context)
                ) { location ->
                    onResult(location ?: fallback)
                }
            } else {
                @Suppress("DEPRECATION")
                manager.requestSingleUpdate(
                    provider,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            onResult(location)
                        }

                        @Deprecated("Deprecated in Android")
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                        override fun onProviderEnabled(provider: String) = Unit

                        override fun onProviderDisabled(provider: String) {
                            onError(LocationError.SERVICES_DISABLED)
                        }
                    },
                    Looper.getMainLooper()
                )
            }
        } catch (_: SecurityException) {
            onError(LocationError.PERMISSION_REQUIRED)
        } catch (_: Exception) {
            onError(LocationError.UNAVAILABLE)
        }
    }
}
