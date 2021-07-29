package de.rki.coronawarnapp.qrcode.provider.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.core.graphics.applyCanvas
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

class PdfUriResolver @Inject constructor(
    private val dispatcherProvider: DispatcherProvider
) {
    private fun Int.toPx(dpi: Int): Int =
        (0.014 * this * dpi).roundToInt()

    // This should be fine because of withContext
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun resolve(uri: Uri, context: Context): List<Bitmap> = withContext(dispatcherProvider.IO) {
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalArgumentException("The file descriptor failed")

        val renderer = PdfRenderer(descriptor)

        // Render all pages into a bitmap
        val bitmaps = (0 until renderer.pageCount).map { pageIndex ->
            val page = renderer.openPage(pageIndex)

            val bitmap = Bitmap.createBitmap(
                page.width.toPx(DPI_FACTOR),
                page.height.toPx(DPI_FACTOR),
                Bitmap.Config.ARGB_8888
            )

            bitmap.applyCanvas {
                drawColor(Color.WHITE)
            }

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            page.close()
            bitmap
        }

        renderer.close()

        bitmaps
    }

    companion object {
        const val DPI_FACTOR = 240
    }
}
