package de.rki.coronawarnapp.ui.presencetracing.attendee.scan

import de.rki.coronawarnapp.util.ui.LazyString

sealed class ScanCheckInQrCodeNavigation {
    object BackNavigation : ScanCheckInQrCodeNavigation()
    data class InvalidQrCode(val errorText: LazyString) : ScanCheckInQrCodeNavigation()
    data class ScanResultNavigation(val uri: String) : ScanCheckInQrCodeNavigation()
}
