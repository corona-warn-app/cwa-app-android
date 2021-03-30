package de.rki.coronawarnapp.ui.eventregistration.organizer.details

sealed class QrCodeDetailNavigationEvents {
    object NavigateBack : QrCodeDetailNavigationEvents()
    object NavigateToPrintFragment : QrCodeDetailNavigationEvents()
    object NavigateToDuplicateFragment : QrCodeDetailNavigationEvents()
}
