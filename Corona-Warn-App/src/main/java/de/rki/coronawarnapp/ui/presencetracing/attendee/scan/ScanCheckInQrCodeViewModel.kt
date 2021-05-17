package de.rki.coronawarnapp.ui.presencetracing.attendee.scan

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

class ScanCheckInQrCodeViewModel @AssistedInject constructor(
    private val qrCodeUriParser: QRCodeUriParser,
    private val cameraSettings: CameraSettings,
    private val traceLocationVerifier: TraceLocationVerifier
) : CWAViewModel() {
    val events = SingleLiveEvent<ScanCheckInQrCodeNavigation>()

    fun onNavigateUp() {
        events.postValue(ScanCheckInQrCodeNavigation.BackNavigation)
    }

    fun onScanResult(barcodeResult: BarcodeResult) = launch {
        try {
            Timber.i("uri: $barcodeResult.result.text")
            val qrCodePayload = qrCodeUriParser.getQrCodePayload(barcodeResult.result.text)
            when (val verifyResult = traceLocationVerifier.verifyTraceLocation(qrCodePayload)) {
                is TraceLocationVerifier.VerificationResult.Invalid ->
                    events.postValue(
                        ScanCheckInQrCodeNavigation.InvalidQrCode(
                            verifyResult.errorTextRes.toResolvingString()
                        )
                    )
                else -> events.postValue(ScanCheckInQrCodeNavigation.ScanResultNavigation(barcodeResult.result.text))
            }
        } catch (e: Exception) {
            Timber.d(e, "TraceLocation verification failed")
            val msg = e.message ?: "QR-Code was invalid"
            events.postValue(ScanCheckInQrCodeNavigation.InvalidQrCode(msg.toLazyString()))
        }
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ScanCheckInQrCodeViewModel>
}
