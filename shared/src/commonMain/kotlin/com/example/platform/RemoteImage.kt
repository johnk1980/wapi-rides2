package com.example.platform

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import org.jetbrains.compose.resources.decodeToImageBitmap

private sealed interface RemoteImageState {
    data object Loading : RemoteImageState
    data object Failed : RemoteImageState
    class Loaded(val bitmap: ImageBitmap) : RemoteImageState
}

private object RemoteImageLoader {
    private val client: HttpClient by lazy { HttpClient() }
    private val cache = mutableMapOf<String, ImageBitmap>()

    suspend fun load(url: String): RemoteImageState {
        cache[url]?.let { return RemoteImageState.Loaded(it) }
        return try {
            val response: HttpResponse = client.get(url)
            if (!response.status.isSuccess()) {
                RemoteImageState.Failed
            } else {
                val bytes: ByteArray = response.body()
                val bitmap = bytes.decodeToImageBitmap()
                cache[url] = bitmap
                RemoteImageState.Loaded(bitmap)
            }
        } catch (_: Throwable) {
            RemoteImageState.Failed
        }
    }
}

/** Loads an image from a URL (replacement for Coil's SubcomposeAsyncImage). */
@Composable
fun WapiRemoteImage(
    model: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    loading: @Composable () -> Unit = {},
    error: @Composable () -> Unit = {}
) {
    val state by produceState<RemoteImageState>(RemoteImageState.Loading, model) {
        value = RemoteImageState.Loading
        value = RemoteImageLoader.load(model)
    }
    when (val s = state) {
        is RemoteImageState.Loaded -> Image(
            bitmap = s.bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
        RemoteImageState.Failed -> error()
        RemoteImageState.Loading -> loading()
    }
}
