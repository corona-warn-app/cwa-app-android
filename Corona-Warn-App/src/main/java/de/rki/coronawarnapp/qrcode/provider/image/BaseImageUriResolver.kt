package de.rki.coronawarnapp.qrcode.provider.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BaseImageUriResolver @Inject constructor(
    private val dispatcherProvider: DispatcherProvider
) : ImageUriResolver {

    // This should be fine because of withContext
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun resolve(uri: Uri, context: Context): Bitmap = withContext(dispatcherProvider.IO) {
        val imageStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(imageStream)
    }
}
