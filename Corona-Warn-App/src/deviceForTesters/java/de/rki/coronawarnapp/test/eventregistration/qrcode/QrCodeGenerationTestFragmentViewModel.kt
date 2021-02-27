package de.rki.coronawarnapp.test.eventregistration.qrcode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.graphics.pdf.PdfDocument
import android.view.View
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.sharing.FileSharing
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class QrCodeGenerationTestFragmentViewModel @AssistedInject constructor(
    private val dispatcher: DispatcherProvider,
    private val fileSharing: FileSharing,
) : CWAViewModel(dispatcher) {

    val bitmapLiveDate = SingleLiveEvent<Bitmap>()
    val errorLiveDate = SingleLiveEvent<String>()
    val intentLiveDate = SingleLiveEvent<FileSharing.ShareIntentProvider>()

    fun generateQrCode(input: String) = launch(dispatcher.IO) {
        bitmapLiveDate.postValue(encodeAsBitmap(input))
    }

    private fun encodeAsBitmap(input: String, size: Int = 200): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.CHARACTER_SET to Charsets.UTF_8
            )
            MultiFormatWriter().encode(
                input,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            ).toBitmap()
        } catch (e: Exception) {
            Timber.d(e, "Qr Code generation failed")
            errorLiveDate.postValue(e.localizedMessage ?: "Qr Code generation failed")
            null
        }
    }

    fun generatePDF(
        context: Context,
        view: View
    ) = launch(dispatcher.IO) {
        try {
            val dir = File(context.filesDir, "events")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, "CoronaWarnApp-Event.pdf")
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                view.width,
                view.height,
                1
            ).create()
            val page = document.startPage(pageInfo)
            view.draw(page.canvas)
            document.finishPage(page)
            document.writeTo(FileOutputStream(file))
            document.close()
            intentLiveDate.postValue(
                fileSharing.getIntentProvider(file, "Scan and Help")
            )
        } catch (e: Exception) {
            errorLiveDate.postValue(e.localizedMessage ?: "Generating PDF Failed")
            Timber.d(e, "Generating PDF Failed")
        }
    }

    private fun BitMatrix.toBitmap() =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            .apply {
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        val color = if (get(x, y)) BLACK else WHITE
                        setPixel(x, y, color)
                    }
                }
            }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<QrCodeGenerationTestFragmentViewModel>
}
