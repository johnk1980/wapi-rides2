package com.example.platform

import androidx.compose.runtime.Composable
import io.github.jan.supabase.auth.AuthConfig

/** Milliseconds since the Unix epoch (replacement for System.currentTimeMillis()). */
expect fun currentTimeMillis(): Long

/** Local hour of day, 0..23. */
expect fun currentHourOfDay(): Int

/** Current instant as an ISO-8601 string, e.g. 2026-10-12T07:00:00.000Z. */
expect fun nowIsoString(): String

/** URL Supabase should redirect to after email verification. */
expect fun authRedirectUrl(): String

/** Per-platform Supabase Auth settings (Android deep link scheme/host; nothing on web). */
expect fun AuthConfig.configureAuthPlatform()

/** System back handling. No-op on web. */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)

/**
 * Returns a function that opens the platform's image picker. When the user picks an image,
 * [onPicked] receives the raw file bytes and the reported MIME type.
 */
@Composable
expect fun rememberImagePickerLauncher(onPicked: (ByteArray, String?) -> Unit): () -> Unit

/**
 * Decodes [rawBytes] (JPEG/PNG/WEBP), center-crops to a square, resizes to the avatar size and
 * returns JPEG bytes. Throws AvatarImageException when the image cannot be decoded.
 */
internal expect fun encodeAvatarJpeg(rawBytes: ByteArray): ByteArray
