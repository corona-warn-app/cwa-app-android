package de.rki.coronawarnapp.ui.presencetracing.organizer.details

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationCategory

sealed class QrCodeDetailNavigationEvents {
    object NavigateBack : QrCodeDetailNavigationEvents()
    data class NavigateToQrCodePosterFragment(val locationId: Long) : QrCodeDetailNavigationEvents()
    data class NavigateToDuplicateFragment(val traceLocation: TraceLocation, val category: TraceLocationCategory) :
        QrCodeDetailNavigationEvents()

    data class NavigateToFullScreenQrCode(
        val qrcodeText: String,
        val correctionLevel: ErrorCorrectionLevel
    ) : QrCodeDetailNavigationEvents()
}
