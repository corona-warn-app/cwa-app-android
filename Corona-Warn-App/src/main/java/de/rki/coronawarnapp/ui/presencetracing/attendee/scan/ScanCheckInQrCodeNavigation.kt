package de.rki.coronawarnapp.ui.presencetracing.attendee.scan

sealed class ScanCheckInQrCodeNavigation {
    object BackNavigation : ScanCheckInQrCodeNavigation()
    data class ScanResultNavigation(val uri: String) : ScanCheckInQrCodeNavigation()
}
