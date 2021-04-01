package de.rki.coronawarnapp.ui.eventregistration.organizer.details

sealed class QrCodeDetailNavigationEvents {
    object NavigateBack : QrCodeDetailNavigationEvents()
    data class NavigateToQrCodePosterFragment(val locationId: Long) : QrCodeDetailNavigationEvents()
    object NavigateToDuplicateFragment : QrCodeDetailNavigationEvents()
}
