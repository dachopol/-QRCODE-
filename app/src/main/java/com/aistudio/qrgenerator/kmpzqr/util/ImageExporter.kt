package com.aistudio.qrgenerator.kmpzqr.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageExporter {

    /**
     * Saves a bitmap to the device Pictures gallery under "QuickQR_Business".
     * Returns the saved content URI, or null on failure.
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Uri? {
        val filename = "QR_${System.currentTimeMillis()}.png"
        var outputStream: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/QuickQR_Business"
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    outputStream = resolver.openOutputStream(imageUri)
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
            } else {
                val picturesDir =
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        ?: context.filesDir
                val appDir = File(picturesDir, "QuickQR_Business")
                if (!appDir.exists()) {
                    appDir.mkdirs()
                }
                val imageFile = File(appDir, filename)
                outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                imageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
            }
        } catch (_: Exception) {
            imageUri = null
        } finally {
            outputStream?.close()
        }

        return imageUri
    }

    /**
     * Shares a bitmap via Android Share Sheet (LINE, Facebook, Chat, etc.).
     */
    fun shareBitmap(context: Context, bitmap: Bitmap, shareText: String) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "shared_qr_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, localizedNow("แชร์ QR", "Share QR")))
        } catch (_: Exception) {
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(textIntent, localizedNow("แชร์ข้อมูล", "Share data")))
        }
    }
}
