package com.example.platform

import com.example.data.AvatarImageException
import com.example.data.AvatarImageProcessor
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.FilterMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.MipmapMode
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import kotlin.math.min

internal actual fun encodeAvatarJpeg(rawBytes: ByteArray): ByteArray {
    val target = AvatarImageProcessor.TARGET_DIMENSION_PX
    val maxBytes = AvatarImageProcessor.MAX_OUTPUT_SIZE_BYTES

    val image = Image.makeFromEncoded(rawBytes)
    if (image.width <= 0 || image.height <= 0) {
        throw AvatarImageException("Could not decode the selected image. Please choose another photo.")
    }

    // Center-crop to a square and scale to the target size on a white background.
    val side = min(image.width, image.height)
    val left = (image.width - side) / 2f
    val top = (image.height - side) / 2f
    val surface = Surface.makeRasterN32Premul(target, target)
    val canvas = surface.canvas
    canvas.clear(0xFFFFFFFF.toInt())
    canvas.drawImageRect(
        image,
        Rect.makeXYWH(left, top, side.toFloat(), side.toFloat()),
        Rect.makeWH(target.toFloat(), target.toFloat()),
        FilterMipmap(FilterMode.LINEAR, MipmapMode.LINEAR),
        null,
        true
    )
    val snapshot = surface.makeImageSnapshot()

    var quality = 85
    while (true) {
        val data = snapshot.encodeToData(EncodedImageFormat.JPEG, quality)
            ?: throw AvatarImageException("Failed to process the selected image.")
        val bytes = data.bytes
        if (bytes.size <= maxBytes || quality <= 40) return bytes
        quality -= 10
    }
}
