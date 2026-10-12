package com.example.data

import com.example.platform.encodeAvatarJpeg

data class ProcessedAvatarImage(
    val bytes: ByteArray,
    val mimeType: String = "image/jpeg",
    val width: Int = 512,
    val height: Int = 512
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProcessedAvatarImage) return false
        return bytes.contentEquals(other.bytes) &&
            mimeType == other.mimeType &&
            width == other.width &&
            height == other.height
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}

class AvatarImageException(override val message: String) : IllegalArgumentException(message)

object AvatarImageProcessor {
    const val MAX_INPUT_SIZE_BYTES: Int = 10 * 1024 * 1024 // 10 MB
    const val MAX_OUTPUT_SIZE_BYTES: Int = 1 * 1024 * 1024 // 1 MB
    const val TARGET_DIMENSION_PX: Int = 512

    val ALLOWED_INPUT_MIME_TYPES: Set<String> = setOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp"
    )

    fun processImageBytes(
        rawBytes: ByteArray,
        mimeType: String?
    ): Result<ProcessedAvatarImage> = runCatching {
        if (rawBytes.isEmpty()) {
            throw AvatarImageException("The selected image is empty or unreadable.")
        }
        if (rawBytes.size > MAX_INPUT_SIZE_BYTES) {
            throw AvatarImageException("Selected image is too large. Please choose an image under 10 MB.")
        }

        val normalizedMime = mimeType?.trim()?.lowercase()
        val resolvedMime = if (normalizedMime != null && normalizedMime in ALLOWED_INPUT_MIME_TYPES) {
            normalizedMime
        } else {
            sniffImageMimeType(rawBytes)?.takeIf { it in ALLOWED_INPUT_MIME_TYPES }
        } ?: throw AvatarImageException("Unsupported image format. Please choose a JPEG, PNG, or WEBP image.")

        // Verify magic header bytes match a valid JPEG, PNG, or WEBP container
        val sniffedMime = sniffImageMimeType(rawBytes)
        if (sniffedMime == null || sniffedMime !in ALLOWED_INPUT_MIME_TYPES) {
            throw AvatarImageException("Could not decode the selected image. Please choose a valid JPEG, PNG, or WEBP photo.")
        }

        val compressedBytes = try {
            encodeAvatarJpeg(rawBytes)
        } catch (e: AvatarImageException) {
            throw e
        } catch (_: Throwable) {
            throw AvatarImageException("Could not decode the selected image. Please choose another photo.")
        }

        if (compressedBytes.isEmpty()) {
            throw AvatarImageException("Failed to process the selected image.")
        }
        if (compressedBytes.size > MAX_OUTPUT_SIZE_BYTES) {
            throw AvatarImageException("Processed image exceeds the 1 MB size limit. Please choose another photo.")
        }

        ProcessedAvatarImage(
            bytes = compressedBytes,
            mimeType = "image/jpeg",
            width = TARGET_DIMENSION_PX,
            height = TARGET_DIMENSION_PX
        )
    }

    internal fun resolveAllowedMimeType(reportedMimeType: String?, rawBytes: ByteArray): String? {
        val cleanMime = reportedMimeType?.trim()?.lowercase()
        if (cleanMime != null && cleanMime in ALLOWED_INPUT_MIME_TYPES) {
            return cleanMime
        }
        return sniffImageMimeType(rawBytes)?.takeIf { it in ALLOWED_INPUT_MIME_TYPES }
    }

    internal fun sniffImageMimeType(bytes: ByteArray): String? {
        if (bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()
        ) {
            return "image/jpeg"
        }
        if (bytes.size >= 8 &&
            bytes[0] == 0x89.toByte() &&
            bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() &&
            bytes[3] == 0x47.toByte() &&
            bytes[4] == 0x0D.toByte() &&
            bytes[5] == 0x0A.toByte() &&
            bytes[6] == 0x1A.toByte() &&
            bytes[7] == 0x0A.toByte()
        ) {
            return "image/png"
        }
        if (bytes.size >= 12 &&
            bytes[0] == 'R'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() &&
            bytes[11] == 'P'.code.toByte()
        ) {
            return "image/webp"
        }
        return null
    }
}
