package de.rki.coronawarnapp.ui.qrcode.fullscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import timber.log.Timber

class QrCodeFullScreenViewModel @AssistedInject constructor(
    @Assisted private val qrCode: CoilQrCode,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    private val qrCodeRequestInternal = MutableLiveData<CoilQrCode>()
    val qrCodeRequest: LiveData<CoilQrCode> = qrCodeRequestInternal
    val immersiveMode = SingleLiveEvent<Boolean>()

    init {
        generateQrCode()
    }

    private fun generateQrCode() = launch {
        try {
            qrCodeRequestInternal.postValue(qrCode)
        } catch (e: Exception) {
            Timber.d(e, "generateQrCode failed")
        }
    }

    fun switchImmersiveMode() = immersiveMode.run { value = !(value ?: false) }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<QrCodeFullScreenViewModel> {
        fun create(qrCode: CoilQrCode): QrCodeFullScreenViewModel
    }
}
