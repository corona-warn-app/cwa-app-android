package de.rki.coronawarnapp.ui.eventregistration.organizer.qrinfo

sealed class TraceLocationQRInfoNavigationEvents {
    object NavigateToDataPrivacy : TraceLocationQRInfoNavigationEvents()
    object NavigateToMyQrCodes : TraceLocationQRInfoNavigationEvents()
}
