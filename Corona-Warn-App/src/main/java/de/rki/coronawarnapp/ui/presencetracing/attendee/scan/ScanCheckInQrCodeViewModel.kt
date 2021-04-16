package de.rki.coronawarnapp.ui.presencetracing.attendee.scan

import com.journeyapps.barcodescanner.BarcodeResult
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class ScanCheckInQrCodeViewModel @AssistedInject constructor(
    private val cameraSettings: CameraSettings
) : CWAViewModel() {
    val events = SingleLiveEvent<ScanCheckInQrCodeNavigation>()

    fun onNavigateUp() {
        events.value = ScanCheckInQrCodeNavigation.BackNavigation
    }

    fun onScanResult(barcodeResult: BarcodeResult) {
        events.value = ScanCheckInQrCodeNavigation.ScanResultNavigation(
            barcodeResult.result.text
        )
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ScanCheckInQrCodeViewModel>
}
