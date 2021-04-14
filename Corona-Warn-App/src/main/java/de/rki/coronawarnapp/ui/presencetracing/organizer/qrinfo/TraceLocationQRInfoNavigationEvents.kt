package de.rki.coronawarnapp.ui.presencetracing.organizer.qrinfo

sealed class TraceLocationQRInfoNavigationEvents {
    object NavigateToDataPrivacy : TraceLocationQRInfoNavigationEvents()
    object NavigateToMyQrCodes : TraceLocationQRInfoNavigationEvents()
}
