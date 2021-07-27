package de.rki.coronawarnapp.qrcode.provider.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.P)
class NewImageUriResolver @Inject constructor(
    private val dispatcherProvider: DispatcherProvider
) : ImageUriResolver {
    // This should be fine because of withContext
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun resolve(uri: Uri, context: Context): Bitmap = withContext(dispatcherProvider.IO) {
        val bitmapSource = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(
            bitmapSource
        ) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
        }
    }
}
