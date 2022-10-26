package de.rki.coronawarnapp.covidcertificate.recovery.ui.details

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import timber.log.Timber

class RecoveryCertificateDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val containerId: RecoveryCertificateContainerId,
    @Assisted private val fromScanner: Boolean,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
    private val dccValidationRepository: DccValidationRepository,
    @AppScope private val appScope: CoroutineScope
) : CWAViewModel(dispatcherProvider) {
    private var qrCode: CoilQrCode? = null
    val events = SingleLiveEvent<RecoveryCertificateDetailsNavigation>()
    val errors = SingleLiveEvent<Throwable>()
    val recoveryCertificate = recoveryCertificateRepository.findCertificateDetails(containerId).map { certificate ->
        certificate?.also { qrCode = it.qrCodeToDisplay }
    }.asLiveData2()

    fun goBack() = events.postValue(RecoveryCertificateDetailsNavigation.Back)

    fun openFullScreen() = qrCode?.let { events.postValue(RecoveryCertificateDetailsNavigation.FullQrCode(it)) }

    fun recycleRecoveryCertificateConfirmed() = launch(scope = appScope) {
        Timber.d("Recycling Recovery Certificate=$containerId")
        recoveryCertificateRepository.recycleCertificate(containerId)
        events.postValue(RecoveryCertificateDetailsNavigation.ReturnToPersonDetailsAfterRecycling)
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

    fun markAsSeen() = launch(scope = appScope) {
        Timber.v("refreshCertState()")
        recoveryCertificateRepository.acknowledgeState(containerId)
        if (!fromScanner) recoveryCertificateRepository.markAsSeenByUser(containerId)
    }

    fun onExport() = events.postValue(
        RecoveryCertificateDetailsNavigation.Export(containerId)
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<RecoveryCertificateDetailsViewModel> {
        fun create(
            containerId: RecoveryCertificateContainerId,
            fromScanner: Boolean
        ): RecoveryCertificateDetailsViewModel
    }
}
