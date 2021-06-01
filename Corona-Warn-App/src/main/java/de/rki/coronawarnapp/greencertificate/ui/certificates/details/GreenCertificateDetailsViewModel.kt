package de.rki.coronawarnapp.greencertificate.ui.certificates.details

import android.graphics.Bitmap
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class GreenCertificateDetailsViewModel @AssistedInject constructor(
    private val qrCodeGenerator: QrCodeGenerator,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider) {

    private var qrCodeText: String? = null
    private val mutableStateFlow = MutableStateFlow<Bitmap?>(null)
    val qrCode = mutableStateFlow.asLiveData(dispatcherProvider.Default)

    val events = SingleLiveEvent<GreenCertificateDetailsNavigation>()

    fun onClose() = events.postValue(GreenCertificateDetailsNavigation.Back)

    fun openFullScreen() = qrCodeText?.let { events.postValue(GreenCertificateDetailsNavigation.FullQrCode(it)) }

    /* TODO: Adapt to Green Certificate */
    fun generateQrCode() = launch {
        try {
            mutableStateFlow.value = qrCodeGenerator.createQrCode("Sample String")
        } catch (e: Exception) {
            Timber.d(e, "generateQrCode failed for greenCertificate=%s", "Sample Certificate")
            mutableStateFlow.value = null
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<GreenCertificateDetailsViewModel>
}
