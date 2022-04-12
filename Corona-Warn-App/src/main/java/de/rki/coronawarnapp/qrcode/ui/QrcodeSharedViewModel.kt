package de.rki.coronawarnapp.qrcode.ui

import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.VerifiedTraceLocation

/**
 * Shares [VerifiedTraceLocation] between start and end destinations.
 * Shares profile name for family tests
 * Since deep-links do not support sharing Parcelables specially when
 * navigation from Main graph to Nested graph is required. To avoid verifying the location url multiple times in the
 * universal scanner and again in here this ViewModel is used to share the location
 */
class QrcodeSharedViewModel : ViewModel() {

    private val verifiedTraceLocationCache = mutableMapOf<String, VerifiedTraceLocation>()

    private val dccQrCodeCache = mutableMapOf<String, DccQrCode>()

    private val dccTicketingTransactionContextCache = mutableMapOf<String, DccTicketingTransactionContext>()

    var personNameFamilyTest: String = ""

    fun verifiedTraceLocation(locationId: String): VerifiedTraceLocation {
        return verifiedTraceLocationCache.remove(locationId) ?: throw IllegalArgumentException(
            "Location must be provided by putVerifiedTraceLocation() first from start destination"
        )
    }

    fun putVerifiedTraceLocation(
        verifiedTraceLocation: VerifiedTraceLocation
    ) {
        verifiedTraceLocationCache[verifiedTraceLocation.locationIdHex] = verifiedTraceLocation
    }

    fun putDccQrCode(dccQrCode: DccQrCode) {
        dccQrCodeCache[dccQrCode.hash] = dccQrCode
    }

    fun dccQrCode(certificateIdentifier: String): DccQrCode {
        return dccQrCodeCache.remove(certificateIdentifier) ?: throw IllegalArgumentException(
            "DccQrCode must be provided by putDccQrCode first from start destination"
        )
    }

    fun putDccTicketingTransactionContext(transactionContext: DccTicketingTransactionContext) {
        dccTicketingTransactionContextCache[transactionContext.initializationData.subject] = transactionContext
    }

    fun dccTicketingTransactionContext(transactionContextIdentifier: String): DccTicketingTransactionContext {
        return dccTicketingTransactionContextCache.remove(transactionContextIdentifier)
            ?: throw IllegalArgumentException(
                "DccTicketingTransactionContext must be provided " +
                    "by putDccTicketingTransactionContext first from start destination"
            )
    }
}
