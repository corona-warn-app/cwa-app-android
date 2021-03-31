package de.rki.coronawarnapp.ui.eventregistration.organizer.details

sealed class QrCodeDetailNavigationEvents {
    object NavigateBack : QrCodeDetailNavigationEvents()
    data class NavigateToPrintFragment(val qrCode: String) : QrCodeDetailNavigationEvents()
    object NavigateToDuplicateFragment : QrCodeDetailNavigationEvents()
}
