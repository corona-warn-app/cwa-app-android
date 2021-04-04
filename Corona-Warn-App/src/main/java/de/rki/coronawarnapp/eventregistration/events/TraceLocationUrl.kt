package de.rki.coronawarnapp.eventregistration.events

import com.google.common.io.BaseEncoding
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.eventregistration.organizer.details.QrCodeGenerator
import javax.inject.Inject

class TraceLocationUrl @Inject constructor() {

    /**
     * Return a url for [TraceLocation] to be used as an input for [QrCodeGenerator]
     * URL format https://e.coronawarn.app?v=1#QR_CODE_PAYLOAD_BASE64URL
     */
    fun locationUrl(traceLocation: TraceLocation): String {
        val payloadBytes = traceLocation.qrCodePayload().toByteArray()
        val base64Url = BaseEncoding.base64Url().omitPadding().encode(payloadBytes)
        return AUTHORITY.plus(base64Url)
    }

    companion object {
        private const val AUTHORITY = "https://e.coronawarn.app?v=${TraceLocation.VERSION}#"
    }
}
