package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.qrcode

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.util.ui.LazyString

sealed class OrganizerWarnQrCodeNavigation {
    object BackNavigation : OrganizerWarnQrCodeNavigation()
    data class InvalidQrCode(val errorText: LazyString) : OrganizerWarnQrCodeNavigation()
    data class DurationSelectionScreen(val traceLocation: TraceLocation) : OrganizerWarnQrCodeNavigation()
}
