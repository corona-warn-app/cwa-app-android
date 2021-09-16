package de.rki.coronawarnapp.qrcode.ui

import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.VerifiedTraceLocation

/**
 * Shares [VerifiedTraceLocation] between start and end destinations.
 * Since deep-links do not support sharing Parcelables specially when
 * navigation from Main graph to Nested graph is required. To avoid verifying the location url multiple times in the
 * universal scanner and again in here this ViewModel is used to share the location
 */
class VerifiedLocationViewModel : ViewModel() {

    private val verifiedTraceLocationCache = mutableMapOf<String, VerifiedTraceLocation>()

    fun verifiedTraceLocation(locationId: String): VerifiedTraceLocation {
        val verifiedTraceLocation = verifiedTraceLocationCache[locationId] ?: throw IllegalArgumentException(
            "Location must be provided by putVerifiedTraceLocation() first from start destination"
        )

        verifiedTraceLocationCache.remove(locationId)
        return verifiedTraceLocation
    }

    fun putVerifiedTraceLocation(
        verifiedTraceLocation: VerifiedTraceLocation
    ) {
        verifiedTraceLocationCache[verifiedTraceLocation.locationIdHex] = verifiedTraceLocation
    }
}
