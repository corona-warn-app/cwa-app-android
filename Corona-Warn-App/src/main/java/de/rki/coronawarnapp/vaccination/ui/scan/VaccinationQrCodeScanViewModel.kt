package de.rki.coronawarnapp.vaccination.ui.scan

import com.journeyapps.barcodescanner.BarcodeResult
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationQRCodeValidator
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import timber.log.Timber

class VaccinationQrCodeScanViewModel @AssistedInject constructor(
    private val cameraSettings: CameraSettings,
    private val vaccinationQRCodeValidator: VaccinationQRCodeValidator,
    private val vaccinationRepository: VaccinationRepository
) : CWAViewModel() {

    val event = SingleLiveEvent<Event>()

    fun onScanResult(barcodeResult: BarcodeResult) = launch {
        try {
            event.postValue(Event.QrCodeScanInProgress)
            val qrCode = vaccinationQRCodeValidator.validate(barcodeResult.text)
            // TODO crashes
            // val certificate = vaccinationRepository.registerVaccination(qrCode)
            // event.postValue(Event.QrCodeScanSucceeded(certificate.certificateId))
            event.postValue(Event.QrCodeScanSucceeded("Scan succeeded"))
        } catch (e: InvalidHealthCertificateException) {
            event.postValue(Event.QrCodeScanFailed(e.errorMessage))
        }
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    sealed class Event {
        object QrCodeScanInProgress : Event()
        data class QrCodeScanSucceeded(val certificateId: String) : Event()
        data class QrCodeScanFailed(val errorMessage: LazyString) : Event()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<VaccinationQrCodeScanViewModel>
}
