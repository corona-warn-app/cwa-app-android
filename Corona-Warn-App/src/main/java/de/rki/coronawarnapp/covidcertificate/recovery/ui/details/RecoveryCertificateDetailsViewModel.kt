package de.rki.coronawarnapp.covidcertificate.recovery.ui.details

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class RecoveryCertificateDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val containerId: RecoveryCertificateContainerId,
    private val qrCodeGenerator: QrCodeGenerator,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
    private val dccValidationRepository: DccValidationRepository,
    private val certificateProvider: CertificateProvider) : CWAViewModel(dispatcherProvider)
{
    private var qrCodeText: String? = null
    private val bitmapStateData = MutableLiveData<Bitmap>()
    val qrCode: LiveData<Bitmap> = bitmapStateData
    val events = SingleLiveEvent<RecoveryCertificateDetailsNavigation>()
    val errors = SingleLiveEvent<Throwable>()
    val recoveryCertificate = recoveryCertificateRepository.certificates.map { certificates ->
        certificates.find { it.containerId == containerId }?.recoveryCertificate
            .also { generateQrCode(it) }
    }.asLiveData(dispatcherProvider.Default)

    fun onClose() = events.postValue(RecoveryCertificateDetailsNavigation.Back)

    fun openFullScreen() = qrCodeText?.let { events.postValue(RecoveryCertificateDetailsNavigation.FullQrCode(it)) }

    fun onDeleteRecoveryCertificateConfirmed() = launch {
        Timber.d("Removing Recovery Certificate=$containerId")
        recoveryCertificateRepository.deleteCertificate(containerId)
        events.postValue(RecoveryCertificateDetailsNavigation.Back)
    }

    fun getCovidCertificate(): CwaCovidCertificate {
        return runBlocking {
            certificateProvider.findCertificate(containerId)
        }
    }

    fun startValidationRulesDownload() = launch {
        try {
            dccValidationRepository.refresh()
            events.postValue(RecoveryCertificateDetailsNavigation.ValidationStart(containerId))
        } catch (e: Exception) {
            Timber.d(e, "validation rule download failed for covidCertificate=%s", containerId)
            errors.postValue(e)
        }
    }

    private fun generateQrCode(recoveryCertificate: RecoveryCertificate?) = launch {
        try {
            bitmapStateData.postValue(
                recoveryCertificate?.let { certificate ->
                    qrCodeGenerator.createQrCode(certificate.qrCode.also { qrCodeText = it })
                }
            )
        } catch (e: Exception) {
            Timber.d(e, "generateQrCode failed for covidCertificate=%s", containerId)
            bitmapStateData.postValue(null)
            errors.postValue(e)
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<RecoveryCertificateDetailsViewModel> {
        fun create(containerId: RecoveryCertificateContainerId): RecoveryCertificateDetailsViewModel
    }
}
