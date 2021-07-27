package de.rki.coronawarnapp.qrcode.provider

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import dagger.Reusable
import de.rki.coronawarnapp.qrcode.provider.image.ImageUriResolver
import de.rki.coronawarnapp.qrcode.provider.pdf.PdfUriResolver
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Reusable
class QRCodeBitmapProvider @Inject constructor(
    @AppContext private val context: Context,
    private val imageUriResolver: ImageUriResolver,
    private val pdfUriResolver: PdfUriResolver
) {
    suspend fun getBitmapsForUri(uri: Uri): BitmapResult {
        val type = context.contentResolver.getType(uri) ?: return BitmapResult.Failed(
            IllegalArgumentException("File uri could not be resolved to a type")
        )

        return try {
            when {
                type.startsWith("image/") ->
                    BitmapResult.Success(listOf(imageUriResolver.resolve(uri, context)))
                type == "application/pdf" ->
                    BitmapResult.Success(pdfUriResolver.resolve(uri, context))
                else ->
                    BitmapResult.Failed(IllegalArgumentException("File is of a not supported type"))
            }
        } catch (ex: Exception) {
            BitmapResult.Failed(ex)
        }
    }

    sealed class BitmapResult {
        data class Success(val bitmaps: List<Bitmap>) : BitmapResult()
        data class Failed(val error: Exception) : BitmapResult()
    }
}
