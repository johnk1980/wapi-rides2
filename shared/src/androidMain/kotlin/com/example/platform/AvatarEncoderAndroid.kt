package com.example.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import com.example.data.AvatarImageException
import com.example.data.AvatarImageProcessor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal actual fun encodeAvatarJpeg(rawBytes: ByteArray): ByteArray {
    val target = AvatarImageProcessor.TARGET_DIMENSION_PX
    val maxBytes = AvatarImageProcessor.MAX_OUTPUT_SIZE_BYTES

    // 1. Decode bounds first to prevent OutOfMemoryError on very large camera photos
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, boundsOptions)
    if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
        throw AvatarImageException("Could not decode the selected image. Please choose another photo.")
    }
    val sampleSize = calculateInSampleSize(boundsOptions.outWidth, boundsOptions.outHeight, target * 2)
    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions)
        ?: throw AvatarImageException("Could not decode the selected image. Please choose another photo.")
    if (decoded.width <= 0 || decoded.height <= 0) {
        decoded.recycle()
        throw AvatarImageException("Could not decode the selected image. Please choose another photo.")
    }

    // 2. EXIF orientation, 3. square crop, 4. resize
    val oriented = applyExifOrientation(decoded, rawBytes)
    val square = centerCropToSquare(oriented)
    val resized = if (square.width == target && square.height == target) {
        square
    } else {
        val scaled = Bitmap.createScaledBitmap(square, target, target, true)
        if (scaled !== square) square.recycle()
        scaled
    }

    // 5. Flatten transparency onto white, 6. compress under the size limit
    val opaque = ensureOpaque(resized)
    val bytes = compressToJpegUnderLimit(opaque, maxBytes)
    opaque.recycle()
    return bytes
}

private fun calculateInSampleSize(width: Int, height: Int, reqDimension: Int): Int {
    var inSampleSize = 1
    val minSide = min(width, height)
    if (minSide > reqDimension) {
        val halfSide = minSide / 2
        while ((halfSide / inSampleSize) >= reqDimension) inSampleSize *= 2
    }
    return max(1, inSampleSize)
}

private fun applyExifOrientation(bitmap: Bitmap, rawBytes: ByteArray): Bitmap {
    return try {
        val exif = ExifInterface(ByteArrayInputStream(rawBytes))
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.preScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.preScale(-1f, 1f)
            }
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) bitmap.recycle()
        rotated
    } catch (_: Exception) {
        bitmap
    }
}

private fun centerCropToSquare(source: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    if (width == height) return source
    val side = min(width, height)
    val xOffset = ((width - side) / 2f).roundToInt().coerceAtLeast(0)
    val yOffset = ((height - side) / 2f).roundToInt().coerceAtLeast(0)
    val cropped = Bitmap.createBitmap(source, xOffset, yOffset, side, side)
    if (cropped !== source) source.recycle()
    return cropped
}

private fun ensureOpaque(source: Bitmap): Bitmap {
    if (!source.hasAlpha()) return source
    val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)
    canvas.drawColor(android.graphics.Color.WHITE)
    canvas.drawBitmap(source, 0f, 0f, null)
    source.recycle()
    return output
}

private fun compressToJpegUnderLimit(bitmap: Bitmap, maxBytes: Int): ByteArray {
    var quality = 85
    while (quality >= 40) {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        val bytes = out.toByteArray()
        if (bytes.size <= maxBytes || quality == 40) return bytes
        quality -= 10
    }
    val fallback = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fallback)
    return fallback.toByteArray()
}
