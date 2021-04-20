package de.rki.coronawarnapp.ui.eventregistration.organizer

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationCategory
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationUIType
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import okio.ByteString.Companion.decodeBase64

object TraceLocationData {

    private const val CRYPTOGRAPHIC_SEED = "MTIzNA=="

    private const val PUB_KEY =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0z" +
            "K7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg=="

    val traceLocationSameDate = TraceLocation(
        id = 1,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
        description = "My Birthday Party",
        address = "at my place",
        startDate = 1618805545L.secondsToInstant(),
        endDate = 1618865545L.secondsToInstant(),
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = CRYPTOGRAPHIC_SEED.decodeBase64()!!,
        cnPublicKey = PUB_KEY,
        version = TraceLocation.VERSION
    )

    val traceLocationDifferentDate = TraceLocation(
        id = 2,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
        description = "Your Birthday Party",
        address = "at your place",
        startDate = 1618740005L.secondsToInstant(),
        endDate = 1618865545L.secondsToInstant(),
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = CRYPTOGRAPHIC_SEED.decodeBase64()!!,
        cnPublicKey = PUB_KEY,
        version = TraceLocation.VERSION
    )

    val categoryEvent = TraceLocationCategory(
        TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT,
        TraceLocationUIType.EVENT,
        R.string.tracelocation_organizer_category_cultural_event_title,
        R.string.tracelocation_organizer_category_cultural_event_subtitle
    )
}
