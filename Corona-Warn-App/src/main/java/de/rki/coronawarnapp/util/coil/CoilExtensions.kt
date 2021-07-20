package de.rki.coronawarnapp.util.coil

import android.view.View
import androidx.core.view.isInvisible
import coil.request.ImageRequest

fun ImageRequest.Builder.placeHolderView(
    image: View,
    placeholder: View
) {
    listener(
        onStart = {
            placeholder.isInvisible = false
            image.isInvisible = true
        },
        onSuccess = { request, metadata ->
            placeholder.isInvisible = true
            image.isInvisible = false
        }
    )
}
