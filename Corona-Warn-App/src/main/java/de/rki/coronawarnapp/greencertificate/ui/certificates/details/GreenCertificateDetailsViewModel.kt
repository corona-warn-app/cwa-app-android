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
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
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

    private fun generateQrCode(certificate: VaccinationCertificate?) = launch {
        try {
            mutableStateFlow.value = certificate?.let {
                qrCodeText = it.vaccinationQrCodeString
                qrCodeGenerator.createQrCode(it.vaccinationQrCodeString)
            }
        } catch (e: Exception) {
            Timber.d(e, "generateQrCode failed for vaccinationCertificate=%s", certificate)
            mutableStateFlow.value = null
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<GreenCertificateDetailsViewModel>
}
