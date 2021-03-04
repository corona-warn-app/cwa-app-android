package de.rki.coronawarnapp.ui.eventregistration.scan

sealed class ScanCheckInQrCodeEvent {
    object BackEvent : ScanCheckInQrCodeEvent()
    data class ConfirmCheckInEvent(val url: String) : ScanCheckInQrCodeEvent()
}
