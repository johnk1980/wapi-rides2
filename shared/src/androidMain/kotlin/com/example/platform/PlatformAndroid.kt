package com.example.platform

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.example.SupabaseConnectivityClient
import com.example.data.AvatarImageProcessor
import io.github.jan.supabase.auth.AuthConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Calendar

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun currentHourOfDay(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

actual fun nowIsoString(): String = java.time.Instant.now().toString()

actual fun authRedirectUrl(): String =
    "${SupabaseConnectivityClient.AUTH_DEEP_LINK_SCHEME}://${SupabaseConnectivityClient.AUTH_DEEP_LINK_HOST}"

actual fun AuthConfig.configureAuthPlatform() {
    scheme = SupabaseConnectivityClient.AUTH_DEEP_LINK_SCHEME
    host = SupabaseConnectivityClient.AUTH_DEEP_LINK_HOST
}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
actual fun rememberImagePickerLauncher(onPicked: (ByteArray, String?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnPicked by rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val picked = withContext(Dispatchers.IO) { readPickedImage(context, uri) }
                if (picked != null) currentOnPicked(picked.first, picked.second)
            }
        }
    }
    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

private fun readPickedImage(context: Context, uri: Uri): Pair<ByteArray, String?>? {
    val resolver = context.contentResolver
    val mime = resolver.getType(uri)?.trim()?.lowercase()
    val stream = resolver.openInputStream(uri) ?: return null
    stream.use { input ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        var total = 0
        // Read at most limit + 1 bytes so oversize files are reported as "too large" by the processor.
        val cap = AvatarImageProcessor.MAX_INPUT_SIZE_BYTES + 1
        while (total < cap) {
            val read = input.read(buffer, 0, minOf(buffer.size, cap - total))
            if (read == -1) break
            output.write(buffer, 0, read)
            total += read
        }
        return output.toByteArray() to mime
    }
}
