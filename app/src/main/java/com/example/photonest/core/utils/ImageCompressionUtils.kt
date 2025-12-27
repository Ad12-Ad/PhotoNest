package com.example.photonest.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale

object ImageCompressionUtils {

    @RequiresApi(Build.VERSION_CODES.R)
    fun compressImage(
        context: Context,
        uri: Uri,
        maxLongEdge: Int = 1800,
        quality: Int = 80
    ): File {
        val resolver = context.contentResolver

        // Decode bitmap safely
        val originalBitmap = resolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it)
        } ?: throw IllegalStateException("Failed to decode image")

        // Resize
        val resizedBitmap = resizeBitmap(originalBitmap, maxLongEdge)

        // Output file
        val outputFile = File(
            context.cacheDir,
            "compressed_${System.currentTimeMillis()}.webp"
        )

        FileOutputStream(outputFile).use { out ->
            resizedBitmap.compress(
                Bitmap.CompressFormat.WEBP_LOSSY,
                quality,
                out
            )
        }

        originalBitmap.recycle()
        resizedBitmap.recycle()

        return outputFile
    }

    private fun resizeBitmap(
        bitmap: Bitmap,
        maxLongEdge: Int
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val longEdge = maxOf(width, height)
        if (longEdge <= maxLongEdge) return bitmap

        val scale = maxLongEdge.toFloat() / longEdge
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return bitmap.scale(newWidth, newHeight)
    }
}