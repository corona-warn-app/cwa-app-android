package de.rki.coronawarnapp.covidcertificate.common.scan

import com.journeyapps.barcodescanner.BarcodeResult
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.DccQrCodeValidator
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class DccQrCodeScanViewModel @AssistedInject constructor(
    private val cameraSettings: CameraSettings,
    private val qrCodeValidator: DccQrCodeValidator,
    private val vaccinationRepository: VaccinationRepository,
    private val testCertificateRepository: TestCertificateRepository,
    private val recoveryCertificateRepository: RecoveryCertificateRepository
) : CWAViewModel() {

    val event = SingleLiveEvent<Event>()

    val errorEvent = SingleLiveEvent<Throwable>()

    fun onScanResult(barcodeResult: BarcodeResult) = launch {
        try {
            event.postValue(Event.QrCodeScanInProgress)
            when (val qrCode = qrCodeValidator.validate(barcodeResult.text)) {
                is VaccinationCertificateQRCode -> registerVaccinationCertificate(qrCode)
                is TestCertificateQRCode -> registerTestCertificate(qrCode)
                is RecoveryCertificateQRCode -> registerRecoveryCertificate(qrCode)
            }
        } catch (e: Throwable) {
            errorEvent.postValue(e)
        }
    }

    private suspend fun registerVaccinationCertificate(qrCode: VaccinationCertificateQRCode) {
        val certificate = vaccinationRepository.registerCertificate(qrCode)
        event.postValue(Event.VaccinationQrCodeScanSucceeded(certificate.containerId))
    }

    private suspend fun registerTestCertificate(qrCode: TestCertificateQRCode) {
        throw NotImplementedError("Test certificate found")
        // TODO
//        val certificate = testCertificateRepository.requestCertificate(qrCode)
//        event.postValue(Event.TestQrCodeScanSucceeded(certificate.certificateId))
    }

    private suspend fun registerRecoveryCertificate(qrCode: RecoveryCertificateQRCode) {
        throw NotImplementedError("Recovery certificate found")
        // TODO
//        val certificate = recoveryCertificateRepository.requestCertificate(qrCode)
//        event.postValue(Event.RecoveryQrCodeScanSucceeded(certificate.certificateId))
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    sealed class Event {
        object QrCodeScanInProgress : Event()
        data class VaccinationQrCodeScanSucceeded(val containerId: VaccinationCertificateContainerId) : Event()
        data class TestQrCodeScanSucceeded(val containerId: TestCertificateContainerId) : Event()
        data class RecoveryQrCodeScanSucceeded(val containerId: RecoveryCertificateContainerId) : Event()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccQrCodeScanViewModel>
}
