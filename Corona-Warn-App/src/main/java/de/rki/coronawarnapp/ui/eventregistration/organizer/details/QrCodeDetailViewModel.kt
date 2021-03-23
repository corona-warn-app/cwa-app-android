package de.rki.coronawarnapp.ui.eventregistration.organizer.details

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class QrCodeDetailViewModel @AssistedInject constructor(
    private val dispatcher: DispatcherProvider,
    private val appConfigProvider: AppConfigProvider,
    @AppContext private val context: Context,
) : CWAViewModel() {

    val qrCodeBitmap = SingleLiveEvent<Bitmap>()
    val errorMessage = SingleLiveEvent<String>()

    val routeToScreen: SingleLiveEvent<QrCodeDetailNavigationEvents> = SingleLiveEvent()

    /**
     * Creates a QR Code [Bitmap] ,result is delivered by [qrCodeBitmap]
     */
    fun createQrCode(input: String) = launch(context = dispatcher.IO) {
        qrCodeBitmap.postValue(encodeAsBitmap(input))
    }

    fun onBackButtonPress() {
        routeToScreen.postValue(QrCodeDetailNavigationEvents.NavigateBack)
    }

    private suspend fun encodeAsBitmap(input: String, size: Int = 1000): Bitmap? {
        return try {
            val qrCodeErrorCorrectionLevel = appConfigProvider
                .getAppConfig()
                .presenceTracing
                .qrCodeErrorCorrectionLevel
            Timber.i("QrCodeErrorCorrectionLevel: $qrCodeErrorCorrectionLevel")
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to qrCodeErrorCorrectionLevel
            )
            MultiFormatWriter().encode(
                input,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            ).toBitmap()
        } catch (e: Exception) {
            Timber.d(e, "Qr code creation failed")
            errorMessage.postValue(e.localizedMessage ?: "QR code creation failed")
            null
        }
    }

    private fun BitMatrix.toBitmap() =
        Bitmap.createBitmap(
            context.resources.displayMetrics,
            width,
            height,
            Bitmap.Config.ARGB_8888
        ).apply {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val color = if (get(x, y)) Color.BLACK else Color.WHITE
                    setPixel(x, y, color)
                }
            }
        }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<QrCodeDetailViewModel>
}
