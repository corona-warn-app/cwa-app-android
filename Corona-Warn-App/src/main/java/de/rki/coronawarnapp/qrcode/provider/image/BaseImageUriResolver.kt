package de.rki.coronawarnapp.qrcode.provider.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import timber.log.Timber
import javax.inject.Inject

class BaseImageUriResolver @Inject constructor(

) : ImageUriResolver {
    // Create a sequence of increasingly smaller images
    override fun resolve(uri: Uri, context: Context): Sequence<Bitmap> {
        var scaleFactor = MAX_SCALE_FACTOR
        return generateSequence {
            if (scaleFactor == 0) {
                return@generateSequence null
            }

            val imageStream = context.contentResolver.openInputStream(uri)

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scaleFactor
            }

            Timber.d("Providing bitmap with scale factor: %s", scaleFactor)

            scaleFactor -= 1

            BitmapFactory.decodeStream(imageStream, null, decodeOptions)
        }
    }

    companion object {
        const val MAX_SCALE_FACTOR = 6
    }
}
