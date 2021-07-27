package de.rki.coronawarnapp.qrcode.provider.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PdfUriResolver @Inject constructor(
    private val dispatcherProvider: DispatcherProvider
) {
    // This should be fine because of withContext
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun resolve(uri: Uri, context: Context): List<Bitmap> = withContext(dispatcherProvider.IO) {
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalArgumentException("The file descriptor failed")

        val renderer = PdfRenderer(descriptor)

        // Render all pages into a bitmap
        val bitmaps = (0 until renderer.pageCount).map { pageIndex ->
            val page = renderer.openPage(pageIndex)

            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            page.close()
            bitmap
        }

        renderer.close()

        bitmaps
    }
}
