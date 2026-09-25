package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.GeoPoint
import java.net.URI
import java.net.URLDecoder
import java.util.Locale

object LocationQrUtil {
    fun isUsable(point: GeoPoint): Boolean = point.isValid()

    fun formatCoordinate(value: Double): String =
        String.format(Locale.US, "%.7f", value)

    fun formatPoint(point: GeoPoint): String =
        formatCoordinate(point.latitude) + ", " + formatCoordinate(point.longitude)

    fun buildGeoPayload(point: GeoPoint): String {
        require(point.isValid()) { "Invalid map coordinates" }
        val latitude = formatCoordinate(point.latitude)
        val longitude = formatCoordinate(point.longitude)
        return "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude
    }

    fun parseGeo(raw: String): GeoPoint =
        parseGeoOrNull(raw) ?: throw IllegalArgumentException("Invalid map coordinates")

    fun parseGeoOrNull(raw: String): GeoPoint? {
        val trimmed = raw.trim()
        if (trimmed.startsWith("geo:", ignoreCase = true)) {
            val body = trimmed.substring(4)
            val base = parseCoordinatePair(body.substringBefore("?").substringBefore(";"))
            val query = body.substringAfter("?", "")
            val queryPoint = query.split("&")
                .asSequence()
                .mapNotNull { parameter ->
                    val key = parameter.substringBefore("=").trim()
                    if (!key.equals("q", ignoreCase = true)) null
                    else decodeUrlComponent(parameter.substringAfter("=", ""))
                }
                .mapNotNull(::parseCoordinatePair)
                .firstOrNull()

            if (base != null && base.isValid()) return base
            if (queryPoint != null && queryPoint.isValid()) return queryPoint
            return null
        }

        parseCoordinatePair(trimmed)?.takeIf { it.isValid() }?.let { return it }
        return parseGoogleMapsGeoOrNull(trimmed)
    }

    private fun parseCoordinatePair(value: String): GeoPoint? {
        val cleaned = value.trim().substringBefore("(").trim()
        val match = Regex("""^\s*([-+]?\d+(?:\.\d+)?)\s*,\s*([-+]?\d+(?:\.\d+)?)\s*$""")
            .matchEntire(cleaned) ?: return null
        val latitude = match.groupValues[1].toDoubleOrNull() ?: return null
        val longitude = match.groupValues[2].toDoubleOrNull() ?: return null
        if (!latitude.isFinite() || !longitude.isFinite()) return null
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
        return GeoPoint(latitude, longitude)
    }

    private fun parseGoogleMapsGeoOrNull(raw: String): GeoPoint? {
        val uri = try {
            URI(raw)
        } catch (_: Exception) {
            return null
        }
        val host = uri.host?.lowercase(Locale.US) ?: return null
        val isMapsPath = uri.path.orEmpty().startsWith("/maps", ignoreCase = true)
        if (!isGoogleMapsHost(host) || (!isMapsPath && host != "maps.google.com" && host != "maps.app.goo.gl")) {
            return null
        }

        uri.rawQuery.orEmpty().split("&")
            .asSequence()
            .map { decodeUrlComponent(it.substringAfter("=", "")) }
            .mapNotNull(::parseCoordinatePair)
            .firstOrNull { it.isValid() }
            ?.let { return it }

        val pathAndFragment = listOfNotNull(uri.rawPath, uri.rawFragment).joinToString("/")
        Regex("""@([-+]?\d+(?:\.\d+)?),([-+]?\d+(?:\.\d+)?)""")
            .find(pathAndFragment)
            ?.let { match ->
                parseCoordinatePair(match.groupValues[1] + "," + match.groupValues[2])
                    ?.takeIf { it.isValid() }
                    ?.let { return it }
            }

        Regex("""!3d([-+]?\d+(?:\.\d+)?)!4d([-+]?\d+(?:\.\d+)?)""")
            .find(pathAndFragment)
            ?.let { match ->
                parseCoordinatePair(match.groupValues[1] + "," + match.groupValues[2])
                    ?.takeIf { it.isValid() }
                    ?.let { return it }
            }

        return null
    }

    private fun isGoogleMapsHost(host: String): Boolean =
        host == "maps.google.com" ||
            host == "maps.app.goo.gl" ||
            host == "www.google.com" ||
            host == "google.com" ||
            host == "goo.gl"

    fun openMap(context: Context, point: GeoPoint) {
        if (!point.isValid()) {
            Toast.makeText(
                context,
                localizedNow("พิกัดไม่ถูกต้อง", "Invalid map coordinates"),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val query = formatCoordinate(point.latitude) + "," + formatCoordinate(point.longitude)
        val geoUri = Uri.parse("geo:" + query + "?q=" + Uri.encode(query))
        val webUri = Uri.parse(
            "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query)
        )
        val intents = listOf(
            Intent(Intent.ACTION_VIEW, geoUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            Intent(Intent.ACTION_VIEW, geoUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )

        for (intent in intents) {
            try {
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                // Try the next compatible map/browser handler.
            }
        }

        Toast.makeText(
            context,
            localizedNow("ไม่พบแอปแผนที่หรือเบราว์เซอร์", "No map or browser app available"),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun decodeUrlComponent(value: String): String = try {
        URLDecoder.decode(value, "UTF-8")
    } catch (_: Exception) {
        value
    }
}
