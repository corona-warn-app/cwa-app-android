package de.rki.coronawarnapp.ui.eventregistration.scan

import com.journeyapps.barcodescanner.BarcodeResult
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ScanCheckInQrCodeViewModel @AssistedInject constructor() : CWAViewModel() {
    val navigationEvents = SingleLiveEvent<ScanCheckInQrCodeEvent>()

    fun onNavigateUp() {
        navigationEvents.value = ScanCheckInQrCodeEvent.BackEvent
    }

    fun onScanResult(barcodeResult: BarcodeResult) {
        navigationEvents.value = ScanCheckInQrCodeEvent.ConfirmCheckInEvent(
            barcodeResult.result.text
        )
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ScanCheckInQrCodeViewModel>
}
