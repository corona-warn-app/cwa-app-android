package de.rki.coronawarnapp.ui.eventregistration.organizer.details

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class QrCodeGenerator @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
    @AppContext private val context: Context,
) {

    suspend fun createQrCode(input: String, size: Int = 1000): Bitmap? {

        val qrCodeErrorCorrectionLevel = appConfigProvider
            .getAppConfig()
            .presenceTracing
            .qrCodeErrorCorrectionLevel
        Timber.i("QrCodeErrorCorrectionLevel: $qrCodeErrorCorrectionLevel")
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to qrCodeErrorCorrectionLevel
        )

        return MultiFormatWriter().encode(
            input,
            BarcodeFormat.QR_CODE,
            size,
            size,
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
