package de.rki.coronawarnapp.qrcode.provider.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.core.graphics.applyCanvas
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

class PdfUriResolver @Inject constructor() {

    fun resolve(uri: Uri, context: Context): Sequence<Bitmap> {
        var scaleFactor = MAX_SCALE

        return generateSequence {
            if (scaleFactor == 0) {
                return@generateSequence null
            }

            val scaledDpi = MAX_DPI / scaleFactor

            Timber.d("Yielding pdf pages with scale factor: %s", scaleFactor)

            scaleFactor -= 1

            yieldAllPdfPages(uri, context, scaledDpi)
        }.flatMap { it }
    }

    private fun yieldAllPdfPages(uri: Uri, context: Context, dpi: Int): Sequence<Bitmap> {
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalArgumentException("The file descriptor failed")

        val renderer = PdfRenderer(descriptor)

        var currentPage = 0
        return generateSequence {
            if (currentPage >= renderer.pageCount) {
                return@generateSequence null
            }

            val page = renderer.openPage(currentPage)

            val bitmap = Bitmap.createBitmap(
                page.width.toPx(dpi),
                page.height.toPx(dpi),
                Bitmap.Config.ARGB_8888
            )

            bitmap.applyCanvas {
                drawColor(Color.WHITE)
            }

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            page.close()

            currentPage += 1

            bitmap
        }
    }

    companion object {
        const val MAX_DPI = 240
        const val MAX_SCALE = 4

        private fun Int.toPx(dpi: Int): Int =
            (0.014 * this * dpi).roundToInt()
    }
}
