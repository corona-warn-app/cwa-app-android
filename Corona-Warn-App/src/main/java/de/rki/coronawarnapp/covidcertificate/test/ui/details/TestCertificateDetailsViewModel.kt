package de.rki.coronawarnapp.covidcertificate.test.ui.details

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
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
    @Assisted private val fromScanner: Boolean,
    private val testCertificateRepository: TestCertificateRepository,
    private val dccValidationRepository: DccValidationRepository,
    @AppScope private val appScope: CoroutineScope,
) : CWAViewModel(dispatcherProvider) {

    private var qrCode: CoilQrCode? = null
    val events = SingleLiveEvent<TestCertificateDetailsNavigation>()
    val errors = SingleLiveEvent<Throwable>()
    val covidCertificate = testCertificateRepository.findCertificateDetails(containerId).map { certificate ->
        certificate?.also { qrCode = it.qrCodeToDisplay }
    }.asLiveData2()

    fun goBack() = events.postValue(TestCertificateDetailsNavigation.Back)

    fun openFullScreen() = qrCode?.let { events.postValue(TestCertificateDetailsNavigation.FullQrCode(it)) }

    fun recycleTestCertificateConfirmed() = launch(scope = appScope) {
        Timber.d("Recycling Test Certificate=$containerId")
        testCertificateRepository.recycleCertificate(containerId)
        events.postValue(TestCertificateDetailsNavigation.ReturnToPersonDetailsAfterRecycling)
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

    fun markAsSeen() = launch(scope = appScope) {
        Timber.v("markAsSeen()")
        if (covidCertificate.value?.isNew == true && !fromScanner) {
            testCertificateRepository.markCertificateAsSeenByUser(containerId)
        } else {
            testCertificateRepository.acknowledgeState(containerId)
        }
    }

    fun onExport() = events.postValue(
        TestCertificateDetailsNavigation.Export(containerId)
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<TestCertificateDetailsViewModel> {
        fun create(containerId: TestCertificateContainerId, fromScanner: Boolean): TestCertificateDetailsViewModel
    }
}
