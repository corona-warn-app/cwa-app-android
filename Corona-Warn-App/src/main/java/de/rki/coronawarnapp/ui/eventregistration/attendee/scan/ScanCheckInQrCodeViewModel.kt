package de.rki.coronawarnapp.ui.eventregistration.attendee.scan

import com.journeyapps.barcodescanner.BarcodeResult
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ScanCheckInQrCodeViewModel @AssistedInject constructor() : CWAViewModel() {
    val navigationEvents = SingleLiveEvent<ScanCheckInQrCodeNavigation>()

    fun onNavigateUp() {
        navigationEvents.value = ScanCheckInQrCodeNavigation.BackNavigation
    }

    fun onScanResult(barcodeResult: BarcodeResult) {
        navigationEvents.value = ScanCheckInQrCodeNavigation.ScanResultNavigation(
            barcodeResult.result.text
        )
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ScanCheckInQrCodeViewModel>
}
