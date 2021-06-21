package de.rki.coronawarnapp.covidcertificate.test.ui.details

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber

class TestCertificateDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val containerId: TestCertificateContainerId,
    private val qrCodeGenerator: QrCodeGenerator,
    private val testCertificateRepository: TestCertificateRepository
) : CWAViewModel(dispatcherProvider) {

    private var qrCodeText: String? = null
    private val bitmapStateData = MutableLiveData<Bitmap>()
    val qrCode: LiveData<Bitmap> = bitmapStateData
    val events = SingleLiveEvent<TestCertificateDetailsNavigation>()
    val errors = SingleLiveEvent<Throwable>()
    val covidCertificate = testCertificateRepository.certificates.map { certificates ->
        certificates.find { it.containerId == containerId }?.testCertificate
            .also { generateQrCode(it) }
    }.asLiveData(dispatcherProvider.Default)

    fun onClose() = events.postValue(TestCertificateDetailsNavigation.Back)

    fun openFullScreen() = qrCodeText?.let { events.postValue(TestCertificateDetailsNavigation.FullQrCode(it)) }

    fun onDeleteTestConfirmed() = launch {
        Timber.d("Removing Test Certificate=$containerId")
        testCertificateRepository.deleteCertificate(containerId)
        events.postValue(TestCertificateDetailsNavigation.Back)
    }

    private fun generateQrCode(testCertificate: TestCertificate?) = launch {
        try {
            bitmapStateData.postValue(
                testCertificate?.let { certificate ->
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
    interface Factory : CWAViewModelFactory<TestCertificateDetailsViewModel> {
        fun create(containerId: TestCertificateContainerId): TestCertificateDetailsViewModel
    }
}
