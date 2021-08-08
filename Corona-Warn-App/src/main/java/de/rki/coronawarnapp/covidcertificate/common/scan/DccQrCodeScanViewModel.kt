package de.rki.coronawarnapp.covidcertificate.common.scan

import android.net.Uri
import com.journeyapps.barcodescanner.BarcodeResult
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.DccQrCodeValidator
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.qrcode.QRCodeFileParser
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class DccQrCodeScanViewModel @AssistedInject constructor(
    private val cameraSettings: CameraSettings,
    private val qrCodeValidator: DccQrCodeValidator,
    private val qrCodeFileParser: QRCodeFileParser,
    private val vaccinationRepository: VaccinationRepository,
    private val testCertificateRepository: TestCertificateRepository,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
    private val dscSignatureValidator: DscSignatureValidator,
) : CWAViewModel() {

    val event = SingleLiveEvent<Event>()

    val errorEvent = SingleLiveEvent<Throwable>()

    fun onScanResult(barcodeResult: BarcodeResult) = launch {
        validateQRCode(barcodeResult.text)
    }

    fun onFileSelected(uri: Uri) = launch {
        event.postValue(Event.QrCodeScanInProgress)

        when (val result = qrCodeFileParser.decodeQrCodeFile(uri)) {
            is QRCodeFileParser.QRCodeParseResult.Success ->
                validateQRCode(result.text)
            is QRCodeFileParser.QRCodeParseResult.Failure ->
                errorEvent.postValue(result.exception)
        }
    }

    private suspend fun validateQRCode(qrCodeText: String) {
        try {
            event.postValue(Event.QrCodeScanInProgress)
            val qrCode = qrCodeValidator.validate(qrCodeText)
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
        event.postValue(
            Event.PersonDetailsScreen(
                certificate.personIdentifier.codeSHA256, certificate.containerId
            )
        )
    }

    private suspend fun registerTestCertificate(qrCode: TestCertificateQRCode) {
        val certificate = testCertificateRepository.registerCertificate(qrCode)
        certificate.personIdentifier?.codeSHA256?.let { sha256 ->
            event.postValue(Event.PersonDetailsScreen(sha256, certificate.containerId))
        }
    }

    private suspend fun registerRecoveryCertificate(qrCode: RecoveryCertificateQRCode) {
        val certificate = recoveryCertificateRepository.registerCertificate(qrCode)
        event.postValue(
            Event.PersonDetailsScreen(
                certificate.personIdentifier.codeSHA256, certificate.containerId
            )
        )
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    sealed class Event {
        object QrCodeScanInProgress : Event()
        data class PersonDetailsScreen(
            val codeSHA256: String,
            val containerId: CertificateContainerId
        ) : Event()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccQrCodeScanViewModel>
}
