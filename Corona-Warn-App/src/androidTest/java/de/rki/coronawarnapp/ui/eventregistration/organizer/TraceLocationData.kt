package de.rki.coronawarnapp.ui.eventregistration.organizer

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationCategory
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationUIType
import okio.ByteString.Companion.decodeBase64
import java.time.Instant

object TraceLocationData {

    private const val CRYPTOGRAPHIC_SEED = "MTIzNA=="

    private const val PUB_KEY =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT0z" +
            "K7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg=="

    val traceLocationSameDate = TraceLocation(
        id = 1,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
        description = "Jahrestreffen der deutschen SAP Anwendergruppe",
        address = "Hauptstr. 3, 69115 Heidelberg",
        startDate = Instant.ofEpochSecond(1624291200L),
        endDate = Instant.ofEpochSecond(1624302000L),
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = CRYPTOGRAPHIC_SEED.decodeBase64()!!,
        cnPublicKey = PUB_KEY,
        version = TraceLocation.VERSION
    )

    val traceLocationDifferentDate = TraceLocation(
        id = 2,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
        description = "Event XYZ",
        address = "Otto-Hahn-Str. 3, 123456 Berlin",
        startDate = Instant.ofEpochSecond(1618740005L),
        endDate = Instant.ofEpochSecond(1618865545L),
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
