package com.aistudio.qrgenerator.kmpzqr.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

object GalleryBitmapDecoder {
    // QuickQR memory guard for arbitrary gallery images. This is an app limit,
    // not an Android or QR standard. Runtime regression tests verify QR decode
    // remains functional after this bounded sampling strategy.
    internal const val MAX_DECODE_DIMENSION = 2048

    internal fun calculateInSampleSize(
        width: Int,
        height: Int,
        maxDimension: Int = MAX_DECODE_DIMENSION
    ): Int {
        if (width <= 0 || height <= 0 || maxDimension <= 0) return 1
        var sample = 1
        while (width / sample > maxDimension || height / sample > maxDimension) {
            if (sample > Int.MAX_VALUE / 2) break
            sample *= 2
        }
        return sample
    }

    fun decode(
        resolver: ContentResolver,
        uri: Uri,
        maxDimension: Int = MAX_DECODE_DIMENSION
    ): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val options = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(
                    width = bounds.outWidth,
                    height = bounds.outHeight,
                    maxDimension = maxDimension
                )
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
    }
}
