package de.rki.coronawarnapp.ui.eventregistration.scan

import android.net.Uri

sealed class ScanCheckInQrCodeEvent {
    object BackEvent : ScanCheckInQrCodeEvent()
    data class ConfirmCheckInEvent(val uri: Uri) : ScanCheckInQrCodeEvent()
}
