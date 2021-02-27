package de.rki.coronawarnapp.test.eventregistration.qrcode

import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import androidx.lifecycle.MutableLiveData
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class QrCodeGenerationTestFragmentViewModel @AssistedInject constructor() : CWAViewModel() {

    val bitmapLiveDate = MutableLiveData<Bitmap>()

    fun generateQrCode(input: String) {

        launch {
            bitmapLiveDate.postValue(encodeAsBitmap(input))
        }
    }

    @Throws(WriterException::class)
    fun encodeAsBitmap(input: String, size: Int = 150): Bitmap? {
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
        } catch (e: IllegalArgumentException) {
            Timber.d(e, "Qr Code generation failed")
            null
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
