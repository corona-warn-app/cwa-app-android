package de.rki.coronawarnapp.ui.qrcode.fullscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import timber.log.Timber

class QrCodeFullScreenViewModel @AssistedInject constructor(
    @Assisted private val qrcodeText: String,
    @Assisted private val correctionLevel: ErrorCorrectionLevel,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    private val qrCodeBitmap = MutableLiveData<CoilQrCode>()
    val qrcode: LiveData<CoilQrCode> = qrCodeBitmap
    val immersiveMode = SingleLiveEvent<Boolean>()

    init {
        generateQrCode()
    }

    private fun generateQrCode() = launch {
        try {
            qrCodeBitmap.postValue(
                CoilQrCode(
                    content = qrcodeText,
                    options = QrCodeOptions(correctionLevel = correctionLevel)
                )
            )
        } catch (e: Exception) {
            Timber.d(e, "generateQrCode failed")
        }
    }

    fun switchImmersiveMode() = immersiveMode.run { value = !(value ?: false) }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<QrCodeFullScreenViewModel> {
        fun create(
            qrcodeText: String,
            correctionLevel: ErrorCorrectionLevel
        ): QrCodeFullScreenViewModel
    }
}
