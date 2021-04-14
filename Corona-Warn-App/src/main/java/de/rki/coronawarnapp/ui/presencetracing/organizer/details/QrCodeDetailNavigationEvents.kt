package de.rki.coronawarnapp.ui.presencetracing.organizer.details

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationCategory

sealed class QrCodeDetailNavigationEvents {
    object NavigateBack : QrCodeDetailNavigationEvents()
    data class NavigateToQrCodePosterFragment(val locationId: Long) : QrCodeDetailNavigationEvents()
    data class NavigateToDuplicateFragment(val traceLocation: TraceLocation, val category: TraceLocationCategory) :
        QrCodeDetailNavigationEvents()
}
