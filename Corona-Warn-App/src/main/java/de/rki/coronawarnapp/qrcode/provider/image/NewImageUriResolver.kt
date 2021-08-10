package de.rki.coronawarnapp.qrcode.provider.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import timber.log.Timber
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.P)
class NewImageUriResolver @Inject constructor() : ImageUriResolver {
    // Create a sequence of increasingly smaller images
    override fun resolve(uri: Uri, context: Context): Sequence<Bitmap> {
        var scaleFactor = MAX_SCALE_FACTOR
        return generateSequence {
            if (scaleFactor == 0) {
                return@generateSequence null
            }

            val bitmapSource = ImageDecoder.createSource(context.contentResolver, uri)

            val bitmap = ImageDecoder.decodeBitmap(
                bitmapSource
            ) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.setTargetSampleSize(scaleFactor)
                decoder.isMutableRequired = true
            }

            Timber.d("Providing bitmap with scale factor: %s", scaleFactor)

            scaleFactor -= 1

            bitmap
        }
    }

    companion object {
        const val MAX_SCALE_FACTOR = 6
    }
}
