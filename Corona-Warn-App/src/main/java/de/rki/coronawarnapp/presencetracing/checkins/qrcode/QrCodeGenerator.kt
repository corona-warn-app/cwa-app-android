package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dagger.Reusable
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class QrCodeGenerator @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    @AppContext private val context: Context,
) {

    /**
     * Decodes input String into a QR Code [Bitmap]
     * @param input [String]
     * @param length [Int] QR Code side length
     * @param margin [Int] QR Code side's margin
     * @param correctionLevel [ErrorCorrectionLevel]
     *
     * Note: we cannot use Charsets.UTF_8 as zxing calls toString internally and some android version
     * return the class name and not the charset name
     * @param characterSet [String]
     *
     * @throws [Exception] it could throw [IllegalArgumentException] , [WriterException]
     * or exception while creating the bitmap
     */
    suspend fun createQrCode(
        input: String,
        length: Int = 1000,
        margin: Int = 1,
        correctionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
        characterSet: String = Charsets.UTF_8.name()
    ): Bitmap = withContext(dispatcherProvider.Default) {
        Timber.i("correctionLevel=$correctionLevel")
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to correctionLevel,
            EncodeHintType.MARGIN to margin,
            EncodeHintType.CHARACTER_SET to characterSet
        )
        MultiFormatWriter().encode(
            input,
            BarcodeFormat.QR_CODE,
            length,
            length,
            hints
        ).toBitmap()
    }

    private fun BitMatrix.toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            context.resources.displayMetrics,
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = if (get(x, y)) Color.BLACK else Color.WHITE
                bitmap.setPixel(x, y, color)
            }
        }
        return bitmap
    }
}
