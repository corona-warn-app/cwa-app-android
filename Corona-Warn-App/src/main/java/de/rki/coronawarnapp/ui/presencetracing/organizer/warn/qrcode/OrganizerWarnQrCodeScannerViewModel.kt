package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.qrcode

import com.journeyapps.barcodescanner.BarcodeResult
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QRCodeUriParser
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationVerifier
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.toLazyString
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class OrganizerWarnQrCodeScannerViewModel @AssistedInject constructor(
    private val qrCodeUriParser: QRCodeUriParser,
    private val cameraSettings: CameraSettings,
    private val traceLocationVerifier: TraceLocationVerifier
) : CWAViewModel() {
    val events = SingleLiveEvent<OrganizerWarnQrCodeNavigation>()

    fun onNavigateUp() {
        events.postValue(OrganizerWarnQrCodeNavigation.BackNavigation)
    }

    fun onScanResult(barcodeResult: BarcodeResult) = launch {
        try {
            Timber.i("uri: ${barcodeResult.result.text}")
            val qrCodePayload = qrCodeUriParser.getQrCodePayload(barcodeResult.result.text)
            when (val verifyResult = traceLocationVerifier.verifyTraceLocation(qrCodePayload)) {
                is TraceLocationVerifier.VerificationResult.Invalid ->
                    events.postValue(
                        OrganizerWarnQrCodeNavigation.InvalidQrCode(
                            verifyResult.errorTextRes.toResolvingString()
                        )
                    )
                is TraceLocationVerifier.VerificationResult.Valid -> events.postValue(
                    OrganizerWarnQrCodeNavigation
                        .DurationSelectionScreen(verifyResult.verifiedTraceLocation.traceLocation)
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
