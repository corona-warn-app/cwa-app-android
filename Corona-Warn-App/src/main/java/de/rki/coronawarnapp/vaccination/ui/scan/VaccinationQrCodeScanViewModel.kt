package de.rki.coronawarnapp.vaccination.ui.scan

import com.journeyapps.barcodescanner.BarcodeResult
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.censors.vaccination.CertificateQrCodeCensor
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationQRCodeValidator
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import timber.log.Timber

class VaccinationQrCodeScanViewModel @AssistedInject constructor(
    private val cameraSettings: CameraSettings,
    private val vaccinationQRCodeValidator: VaccinationQRCodeValidator,
    private val vaccinationRepository: VaccinationRepository
) : CWAViewModel() {

    val event = SingleLiveEvent<Event>()

    val errorEvent = SingleLiveEvent<Throwable>()

    fun onScanResult(barcodeResult: BarcodeResult) = launch {
        try {
            event.postValue(Event.QrCodeScanInProgress)
            CertificateQrCodeCensor.dataToCensor =
                CertificateQrCodeCensor.dataToCensor.copy(rawString = barcodeResult.text)
            val qrCode = vaccinationQRCodeValidator.validate(barcodeResult.text)
            val vaccinationCertificate = vaccinationRepository.registerVaccination(qrCode)
            event.postValue(Event.QrCodeScanSucceeded(vaccinationCertificate.personIdentifier.codeSHA256))
        } catch (e: Throwable) {
            errorEvent.postValue(e)
        }
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    sealed class Event {
        object QrCodeScanInProgress : Event()
        data class QrCodeScanSucceeded(val personIdentifierCodeSha256: String) : Event()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<VaccinationQrCodeScanViewModel>
}
