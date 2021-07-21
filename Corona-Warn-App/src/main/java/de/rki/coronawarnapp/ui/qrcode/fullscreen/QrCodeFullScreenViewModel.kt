package de.rki.coronawarnapp.ui.qrcode.fullscreen

import androidx.lifecycle.LiveData
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.qrcode.QrCodeOptions
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class QrCodeFullScreenViewModel @AssistedInject constructor(
    @Assisted private val qrcodeText: String,
    @Assisted private val correctionLevel: ErrorCorrectionLevel,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    val qrcode: LiveData<CoilQrCode> = flow {
        try {
            val qr = CoilQrCode(
                content = qrcodeText,
                options = QrCodeOptions(correctionLevel = correctionLevel)
            )
            emit(qr)
        } catch (e: Exception) {
            Timber.d(e, "generateQrCode failed")
        }
    }.asLiveData2()

    val immersiveMode = SingleLiveEvent<Boolean>()

    fun switchImmersiveMode() = immersiveMode.run { value = !(value ?: false) }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<QrCodeFullScreenViewModel> {
        fun create(
            qrcodeText: String,
            correctionLevel: ErrorCorrectionLevel
        ): QrCodeFullScreenViewModel
    }
}
