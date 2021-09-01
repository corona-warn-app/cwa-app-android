package de.rki.coronawarnapp.covidcertificate.test.ui.details

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.pdf.ui.canBeExported
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
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

class TestCertificateDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val containerId: TestCertificateContainerId,
    private val testCertificateRepository: TestCertificateRepository,
    private val dccValidationRepository: DccValidationRepository,
    @AppScope private val appScope: CoroutineScope
) : CWAViewModel(dispatcherProvider) {

    private var qrCode: CoilQrCode? = null
    val events = SingleLiveEvent<TestCertificateDetailsNavigation>()
    val errors = SingleLiveEvent<Throwable>()

    val exportError = SingleLiveEvent<Unit>()

    val covidCertificate = testCertificateRepository.certificates.map { certificates ->
        certificates.find { it.containerId == containerId }?.testCertificate?.also {
            qrCode = it.qrCodeToDisplay
        }
    }.asLiveData(dispatcherProvider.Default)

    fun onClose() = events.postValue(TestCertificateDetailsNavigation.Back)

    fun openFullScreen() = qrCode?.let { events.postValue(TestCertificateDetailsNavigation.FullQrCode(it)) }

    fun onDeleteTestCertificateConfirmed() = launch {
        Timber.d("Removing Test Certificate=$containerId")
        testCertificateRepository.deleteCertificate(containerId)
        events.postValue(TestCertificateDetailsNavigation.Back)
    }

    fun startValidationRulesDownload() = launch {
        try {
            dccValidationRepository.refresh()
            events.postValue(TestCertificateDetailsNavigation.ValidationStart(containerId))
        } catch (e: Exception) {
            Timber.d(e, "validation rule download failed for covidCertificate=%s", containerId)
            errors.postValue(e)
        }
    }

    fun refreshCertState() = launch(scope = appScope) {
        Timber.v("refreshCertState()")
        testCertificateRepository.acknowledgeState(containerId)
    }

    fun onExport() {
        if (covidCertificate.value?.canBeExported() == false) {
            exportError.postValue(null)
        } else {
            events.postValue(TestCertificateDetailsNavigation.Export(containerId))
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<TestCertificateDetailsViewModel> {
        fun create(containerId: TestCertificateContainerId): TestCertificateDetailsViewModel
    }
}
