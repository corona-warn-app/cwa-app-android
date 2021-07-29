package de.rki.coronawarnapp.covidcertificate.recovery.ui.details

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber

class RecoveryCertificateDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val containerId: RecoveryCertificateContainerId,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
    private val dccValidationRepository: DccValidationRepository,
) : CWAViewModel(dispatcherProvider) {
    private var qrCode: CoilQrCode? = null
    val events = SingleLiveEvent<RecoveryCertificateDetailsNavigation>()
    val errors = SingleLiveEvent<Throwable>()
    val recoveryCertificate = recoveryCertificateRepository.certificates.map { certificates ->
        certificates.find { it.containerId == containerId }?.recoveryCertificate?.also {
            qrCode = it.qrCodeToDisplay
        }
    }.asLiveData(dispatcherProvider.Default)

    fun onClose() = events.postValue(RecoveryCertificateDetailsNavigation.Back)

    fun openFullScreen() = qrCode?.let { events.postValue(RecoveryCertificateDetailsNavigation.FullQrCode(it)) }

    fun onDeleteRecoveryCertificateConfirmed() = launch {
        Timber.d("Removing Recovery Certificate=$containerId")
        recoveryCertificateRepository.deleteCertificate(containerId)
        events.postValue(RecoveryCertificateDetailsNavigation.Back)
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

    @AssistedFactory
    interface Factory : CWAViewModelFactory<RecoveryCertificateDetailsViewModel> {
        fun create(containerId: RecoveryCertificateContainerId): RecoveryCertificateDetailsViewModel
    }
}
