package de.rki.coronawarnapp.greencertificate.ui.certificates.details

import android.graphics.Bitmap
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.TestCertificateRepository
import de.rki.coronawarnapp.coronatest.type.TestCertificateContainer
import de.rki.coronawarnapp.coronatest.type.TestCertificateIdentifier
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class CovidCertificateDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val testCertificateIdentifier: TestCertificateIdentifier,
    private val qrCodeGenerator: QrCodeGenerator,
    private val testCertificateRepository: TestCertificateRepository
) : CWAViewModel(dispatcherProvider) {

    private var qrCodeText: String? = null
    private val mutableStateFlow = MutableStateFlow<Bitmap?>(null)
    val qrCode = mutableStateFlow.asLiveData(dispatcherProvider.Default)
    val events = SingleLiveEvent<CovidCertificateDetailsNavigation>()
    val errors = SingleLiveEvent<Throwable>()
    val covidCertificate = testCertificateRepository.certificates.map {
        findCovidCertificate(it)
    }.asLiveData(dispatcherProvider.Default)

    fun onClose() = events.postValue(CovidCertificateDetailsNavigation.Back)

    fun openFullScreen() = qrCodeText?.let { events.postValue(CovidCertificateDetailsNavigation.FullQrCode(it)) }

    fun generateQrCode() = launch {
        try {
            mutableStateFlow.value = qrCodeGenerator.createQrCode("Sample String".also { qrCodeText = it })
        } catch (e: Exception) {
            Timber.d(e, "generateQrCode failed for covidCertificate=%s", testCertificateIdentifier)
            mutableStateFlow.value = null
            errors.postValue(e)
        }
    }

    fun onDeleteTestConfirmed() = launch {
        try {
            Timber.d("deleteTest")
            testCertificateRepository.deleteCertificate(testCertificateIdentifier)
            events.postValue(CovidCertificateDetailsNavigation.Back)
        } catch (e: Exception) {
            Timber.d(e, "Failed to delete test certificate:$testCertificateIdentifier")
            errors.postValue(e)
        }
    }

    private fun findCovidCertificate(
        certificates: Set<TestCertificateContainer>
    ): TestCertificateContainer? = certificates.find { it.identifier == testCertificateIdentifier }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<CovidCertificateDetailsViewModel> {
        fun create(testCertificateIdentifier: TestCertificateIdentifier): CovidCertificateDetailsViewModel
    }
}
