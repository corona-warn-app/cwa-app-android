package de.rki.coronawarnapp.ui.qrcode.fullscreen

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import timber.log.Timber

class QrCodeFullScreenViewModel @AssistedInject constructor(
    @Assisted private val qrcodeText: String,
    private val qrCodeGenerator: QrCodeGenerator,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    private val qrCodeBitmap = MutableLiveData<Bitmap>()
    val qrcode: LiveData<Bitmap> = qrCodeBitmap
    val immersiveMode = SingleLiveEvent<Boolean>()

    init {
        generateQrCode()
    }

    private fun generateQrCode() = launch {
        try {
            qrCodeBitmap.postValue(qrCodeGenerator.createQrCode(qrcodeText))
        } catch (e: Exception) {
            Timber.d(e, "generateQrCode failed")
        }
    }

    fun switchImmersiveMode() = immersiveMode.run { value = !(value ?: false) }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<QrCodeFullScreenViewModel> {
        fun create(
            qrcodeText: String
        ): QrCodeFullScreenViewModel
    }
}
