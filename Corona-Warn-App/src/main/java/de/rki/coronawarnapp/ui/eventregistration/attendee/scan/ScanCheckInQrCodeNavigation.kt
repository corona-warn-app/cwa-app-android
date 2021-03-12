package de.rki.coronawarnapp.ui.eventregistration.attendee.scan

sealed class ScanCheckInQrCodeNavigation {
    object BackNavigation : ScanCheckInQrCodeNavigation()
    data class ScanResultNavigation(val uri: String) : ScanCheckInQrCodeNavigation()
}
