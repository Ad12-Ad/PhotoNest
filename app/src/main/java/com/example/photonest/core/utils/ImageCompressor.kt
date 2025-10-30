package com.example.photonest.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

object ImageCompressor {

    suspend fun compressImage(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = 1080,
        maxHeight: Int = 1080,
        quality: Int = 80
    ): Uri = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Get image rotation from EXIF data
        val rotation = getImageRotation(context, imageUri)

        // Rotate if needed
        val rotatedBitmap = if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
        } else {
            originalBitmap
        }

        // Calculate new dimensions
        val ratio = min(
            maxWidth.toFloat() / rotatedBitmap.width,
            maxHeight.toFloat() / rotatedBitmap.height
        )

        val newWidth = (rotatedBitmap.width * ratio).toInt()
        val newHeight = (rotatedBitmap.height * ratio).toInt()

        // Resize bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, true)

        // Save compressed image
        val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(compressedFile).use { outputStream ->
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        // Clean up
        if (rotatedBitmap != originalBitmap) {
            rotatedBitmap.recycle()
        }
        originalBitmap.recycle()
        resizedBitmap.recycle()

        Uri.fromFile(compressedFile)
    }

    suspend fun compressBitmap(
        bitmap: Bitmap,
        maxWidth: Int = 1080,
        maxHeight: Int = 1080,
        quality: Int = 80
    ): Bitmap = withContext(Dispatchers.IO) {
        // Calculate new dimensions
        val ratio = min(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )

        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        // Resize bitmap
        Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun getImageRotation(context: Context, imageUri: Uri): Float {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = ExifInterface(inputStream!!)
            inputStream.close()

            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: IOException) {
            0f
        }
    }

    fun getImageSize(context: Context, imageUri: Uri): Pair<Int, Int> {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            Pair(options.outWidth, options.outHeight)
        } catch (e: Exception) {
            Pair(0, 0)
        }
    }

    fun getCompressedSize(originalSize: Long, quality: Int): Long {
        return (originalSize * quality / 100)
    }
}