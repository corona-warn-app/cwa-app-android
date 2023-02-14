package de.rki.coronawarnapp.qrcode.provider

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import de.rki.coronawarnapp.qrcode.scanner.ImportDocumentException
import de.rki.coronawarnapp.qrcode.scanner.ImportDocumentException.ErrorCode.FILE_FORMAT_NOT_SUPPORTED
import de.rki.coronawarnapp.qrcode.provider.image.ImageUriResolver
import de.rki.coronawarnapp.qrcode.provider.pdf.PdfUriResolver
import javax.inject.Inject

@Reusable
class QRCodeBitmapProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageUriResolver: ImageUriResolver,
    private val pdfUriResolver: PdfUriResolver
) {
    fun getBitmapsForUri(uri: Uri): BitmapResult {
        val type = context.contentResolver.getType(uri)
            ?: return BitmapResult.Failed(ImportDocumentException(FILE_FORMAT_NOT_SUPPORTED))

        return try {
            when {
                type.startsWith("image/") ->
                    BitmapResult.Success(imageUriResolver.resolve(uri, context))
                type == "application/pdf" ->
                    BitmapResult.Success(pdfUriResolver.resolve(uri, context))
                else ->
                    BitmapResult.Failed(ImportDocumentException(FILE_FORMAT_NOT_SUPPORTED))
            }
        } catch (ex: Exception) {
            BitmapResult.Failed(ex)
        }
    }

    sealed class BitmapResult {
        data class Success(val bitmaps: Sequence<Bitmap>) : BitmapResult()
        data class Failed(val error: Exception) : BitmapResult()
    }
}
