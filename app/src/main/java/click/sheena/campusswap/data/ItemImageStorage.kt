package click.sheena.campusswap.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.IOException
import java.util.UUID

object ItemImageStorage {

    fun copyImage(context: Context, uri: Uri): String {
        val directory = File(context.filesDir, "item_images")

        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("Cannot create image directory")
        }

        val destination = File(
            directory,
            UUID.randomUUID().toString()
        )

        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Cannot open selected image")

            input.use { source ->
                destination.outputStream().use { target ->
                    source.copyTo(target)
                }
            }

            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeFile(destination.absolutePath, bounds)

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                throw IOException("Cannot decode selected image")
            }

            return destination.absolutePath
        } catch (exception: Exception) {
            destination.delete()
            throw exception
        }
    }

    fun readImage(path: String): Bitmap? {
        if (path.isBlank()) return null

        return try {
            val file = File(path)

            if (!file.isFile) return null

            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeFile(file.absolutePath, bounds)

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null
            }

            var sampleSize = 1

            while (
                bounds.outWidth / sampleSize > 1024 ||
                bounds.outHeight / sampleSize > 1024
            ) {
                sampleSize *= 2
            }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (_: Exception) {
            null
        }
    }
}