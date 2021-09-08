package de.rki.coronawarnapp.covidcertificate.common.scan

import com.journeyapps.barcodescanner.BarcodeResult
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
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
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
    private val dscSignatureValidator: DscSignatureValidator,
) : CWAViewModel() {

    val event = SingleLiveEvent<Event>()

    val errorEvent = SingleLiveEvent<Throwable>()

    fun onScanResult(barcodeResult: BarcodeResult) = launch {
        try {
            event.postValue(Event.QrCodeScanInProgress)
            val qrCode = qrCodeValidator.validate(barcodeResult.text)
            dscSignatureValidator.validateSignature(qrCode.data)
            when (qrCode) {
                is VaccinationCertificateQRCode -> registerVaccinationCertificate(qrCode)
                is TestCertificateQRCode -> registerTestCertificate(qrCode)
                is RecoveryCertificateQRCode -> registerRecoveryCertificate(qrCode)
            }
        } catch (e: Throwable) {
            Timber.d(e, "Scanning Dcc failed")
            errorEvent.postValue(e)
        }
    }

    private suspend fun registerVaccinationCertificate(qrCode: VaccinationCertificateQRCode) {
        val certificate = vaccinationRepository.registerCertificate(qrCode)
        event.postValue(Event.VaccinationCertScreen(certificate.containerId))
    }

    private suspend fun registerTestCertificate(qrCode: TestCertificateQRCode) {
        val certificate = testCertificateRepository.registerCertificate(qrCode)
        event.postValue(Event.TestCertScreen(certificate.containerId))
    }

    private suspend fun registerRecoveryCertificate(qrCode: RecoveryCertificateQRCode) {
        val certificate = recoveryCertificateRepository.registerCertificate(qrCode)
        event.postValue(Event.RecoveryCertScreen(certificate.containerId))
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    sealed class Event {
        object QrCodeScanInProgress : Event()
        data class TestCertScreen(val containerId: TestCertificateContainerId) : Event()
        data class VaccinationCertScreen(val containerId: VaccinationCertificateContainerId) : Event()
        data class RecoveryCertScreen(val containerId: RecoveryCertificateContainerId) : Event()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccQrCodeScanViewModel>
}
