package de.rki.coronawarnapp.util.coil

import android.content.Context
import android.view.View
import androidx.core.view.isInvisible
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode

fun ImageRequest.Builder.loadingView(
    imageView: View,
    loadingView: View
) {
    listener(
        onStart = {
            loadingView.isInvisible = false
            imageView.isInvisible = true
        },
        onSuccess = { request, metadata ->
            loadingView.isInvisible = true
            imageView.isInvisible = false
        }
    )
}

/**
 * Note that the loaded drawable will be scoped to the contexts lifecycle.
 */
suspend fun Context.loadQrCode(qrCode: CoilQrCode, size: Int): ImageResult {
    val req = ImageRequest.Builder(this).apply {
        data(qrCode)
        size(size)
    }.build()
    return imageLoader.execute(req)
}
