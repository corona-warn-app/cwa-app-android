package de.rki.coronawarnapp.covidcertificate.test.ui.details

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.util.QrCodeHelper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class TestCertificateDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val containerId: TestCertificateContainerId,
    private val testCertificateRepository: TestCertificateRepository,
    private val dccValidationRepository: DccValidationRepository,
    private val certificateProvider: CertificateProvider
) : CWAViewModel(dispatcherProvider) {

    private var qrCodeText: String? = null
    val events = SingleLiveEvent<TestCertificateDetailsNavigation>()
    val errors = SingleLiveEvent<Throwable>()
    val covidCertificate = testCertificateRepository.certificates.map { certificates ->
        certificates.find { it.containerId == containerId }?.testCertificate?.also {
            qrCodeText = it.qrCode
        }
    }.asLiveData(dispatcherProvider.Default)

    fun onClose() = events.postValue(TestCertificateDetailsNavigation.Back)

    fun openFullScreen() = qrCodeText?.let { events.postValue(TestCertificateDetailsNavigation.FullQrCode(it)) }

    fun onDeleteTestCertificateConfirmed() = launch {
        Timber.d("Removing Test Certificate=$containerId")
        testCertificateRepository.deleteCertificate(containerId)
        events.postValue(TestCertificateDetailsNavigation.Back)
    }

    fun getCovidCertificate(): CwaCovidCertificate {
        return runBlocking {
            certificateProvider.findCertificate(containerId)
        }
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

    @AssistedFactory
    interface Factory : CWAViewModelFactory<TestCertificateDetailsViewModel> {
        fun create(containerId: TestCertificateContainerId): TestCertificateDetailsViewModel
    }
}
