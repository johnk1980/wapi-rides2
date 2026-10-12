package com.example.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.github.jan.supabase.auth.AuthConfig

@JsFun("() => Date.now()")
private external fun jsNow(): Double

@JsFun("() => new Date().getHours()")
private external fun jsHour(): Int

@JsFun("() => new Date().toISOString()")
private external fun jsIso(): String

@JsFun("() => window.location.origin + window.location.pathname")
private external fun jsPageUrl(): String

/**
 * Opens a hidden <input type=file>. When a file is chosen, calls [onPicked] with the file contents
 * as a "binary string" (one char per byte) and the MIME type.
 */
@JsFun(
    """(onPicked) => {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'image/jpeg,image/png,image/webp,image/*';
        input.style.display = 'none';
        input.onchange = () => {
            const file = input.files && input.files[0];
            if (input.parentNode) { input.parentNode.removeChild(input); }
            if (!file) { return; }
            const reader = new FileReader();
            reader.onload = () => {
                const bytes = new Uint8Array(reader.result);
                let binary = '';
                const chunk = 0x8000;
                for (let i = 0; i < bytes.length; i += chunk) {
                    binary += String.fromCharCode.apply(null, bytes.subarray(i, i + chunk));
                }
                onPicked(binary, file.type || '');
            };
            reader.readAsArrayBuffer(file);
        };
        document.body.appendChild(input);
        input.click();
    }"""
)
private external fun jsPickImage(onPicked: (String, String) -> Unit)

actual fun currentTimeMillis(): Long = jsNow().toLong()

actual fun currentHourOfDay(): Int = jsHour()

actual fun nowIsoString(): String = jsIso()

actual fun authRedirectUrl(): String = jsPageUrl()

actual fun AuthConfig.configureAuthPlatform() {
    // Nothing to configure on the web: Supabase redirects back to the page URL.
}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // The browser/PWA has no system back button handled by the app; on-screen back buttons are used.
}

@Composable
actual fun rememberImagePickerLauncher(onPicked: (ByteArray, String?) -> Unit): () -> Unit {
    val currentOnPicked by rememberUpdatedState(onPicked)
    return remember {
        val launch: () -> Unit = {
            jsPickImage { binary, mime ->
                val bytes = ByteArray(binary.length) { index -> binary[index].code.toByte() }
                currentOnPicked(bytes, mime.ifBlank { null })
            }
        }
        launch
    }
}
