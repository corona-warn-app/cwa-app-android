package de.rki.coronawarnapp.qrcode.provider.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

interface ImageUriResolver {
    fun resolve(uri: Uri, context: Context): Sequence<Bitmap>
}
