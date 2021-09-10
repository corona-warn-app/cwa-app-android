package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.qrcode

import com.journeyapps.barcodescanner.BarcodeResult
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCodeExtractor
import de.rki.coronawarnapp.qrcode.handler.CheckInQrCodeHandler
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.toLazyString
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class OrganizerWarnQrCodeScannerViewModel @AssistedInject constructor(
    private val checkInQrCodeExtractor: CheckInQrCodeExtractor,
    private val cameraSettings: CameraSettings,
    private val checkInQrCodeHandler: CheckInQrCodeHandler
) : CWAViewModel() {
    val events = SingleLiveEvent<OrganizerWarnQrCodeNavigation>()

    fun onNavigateUp() {
        events.postValue(OrganizerWarnQrCodeNavigation.BackNavigation)
    }

    fun onScanResult(barcodeResult: BarcodeResult) = launch {
        try {
            Timber.i("uri: ${barcodeResult.result.text}")
            val qrCodePayload = checkInQrCodeExtractor.extract(barcodeResult.result.text).qrCodePayload
            when (val result = checkInQrCodeHandler.handleCheckInQrCode(qrCodePayload)) {
                is CheckInQrCodeHandler.Result.Invalid -> events.postValue(
                    OrganizerWarnQrCodeNavigation.InvalidQrCode(result.errorTextRes.toResolvingString())
                )
                is CheckInQrCodeHandler.Result.Valid -> events.postValue(
                    OrganizerWarnQrCodeNavigation.DurationSelectionScreen(result.verifiedTraceLocation.traceLocation)
                )
            }
        } catch (e: Exception) {
            Timber.d(e, "TraceLocation verification failed")
            val msg = e.message ?: "QR-Code was invalid"
            events.postValue(OrganizerWarnQrCodeNavigation.InvalidQrCode(msg.toLazyString()))
        }
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OrganizerWarnQrCodeScannerViewModel>
}
