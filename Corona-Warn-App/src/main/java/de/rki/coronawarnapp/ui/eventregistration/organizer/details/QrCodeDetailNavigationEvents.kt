package de.rki.coronawarnapp.ui.eventregistration.organizer.details

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation

sealed class QrCodeDetailNavigationEvents {
    object NavigateBack : QrCodeDetailNavigationEvents()
    data class NavigateToQrCodePosterFragment(val locationId: Long) : QrCodeDetailNavigationEvents()
    data class NavigateToDuplicateFragment(val traceLocation: TraceLocation) : QrCodeDetailNavigationEvents()
}
