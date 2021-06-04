package de.rki.coronawarnapp.greencertificate.ui.certificates.details

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.TestCertificateRepository
import de.rki.coronawarnapp.coronatest.type.TestCertificateIdentifier
import de.rki.coronawarnapp.covidcertificate.test.TestCertificate
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber

class CovidCertificateDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val testCertificateIdentifier: TestCertificateIdentifier,
    private val qrCodeGenerator: QrCodeGenerator,
    private val testCertificateRepository: TestCertificateRepository
) : CWAViewModel(dispatcherProvider) {

    private var qrCodeText: String? = null
    private val bitmapStateData = MutableLiveData<Bitmap>()
    val qrCode: LiveData<Bitmap> = bitmapStateData
    val events = SingleLiveEvent<CovidCertificateDetailsNavigation>()
    val errors = SingleLiveEvent<Throwable>()
    val covidCertificate = testCertificateRepository.certificates.map { certificates ->
        certificates.find { it.identifier == testCertificateIdentifier }?.toTestCertificate()
            .also { generateQrCode(it) }
    }.asLiveData(dispatcherProvider.Default)

    fun onClose() = events.postValue(CovidCertificateDetailsNavigation.Back)

    fun openFullScreen() = qrCodeText?.let { events.postValue(CovidCertificateDetailsNavigation.FullQrCode(it)) }

    fun onDeleteTestConfirmed() = launch {
        Timber.d("Removing Test Certificate=$testCertificateIdentifier")
        testCertificateRepository.deleteCertificate(testCertificateIdentifier)
        events.postValue(CovidCertificateDetailsNavigation.Back)
    }

    private fun generateQrCode(testCertificate: TestCertificate?) = launch {
        try {
            bitmapStateData.postValue(
                testCertificate?.let { certificate ->
                    qrCodeGenerator.createQrCode(certificate.qrCode.also { qrCodeText = it })
                }
            )
        } catch (e: Exception) {
            Timber.d(e, "generateQrCode failed for covidCertificate=%s", testCertificateIdentifier)
            bitmapStateData.postValue(null)
            errors.postValue(e)
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<CovidCertificateDetailsViewModel> {
        fun create(testCertificateIdentifier: TestCertificateIdentifier): CovidCertificateDetailsViewModel
    }
}
