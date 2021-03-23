package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.eventregistration.events.server.TraceLocationData
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class SignedTraceLocationToTraceLocationMapperTest : BaseTest() {

    @Test
    fun `SignedTraceLocation_toTraceLocation() should map temporary event correctly`() {
        TraceLocationData.signedTraceLocationTemporary.toTraceLocation() shouldBe TraceLocation(
            guid = "serverGeneratedGuid",
            version = 1,
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
            description = "Event Registration Release Party",
            address = "SAP Headquarter",
            startDate = Instant.parse("2021-05-01T19:00:00.000Z"),
            endDate = Instant.parse("2021-05-01T23:30:00.000Z"),
            byteRepresentation = TraceLocationData.signedTraceLocationTemporary.location.toByteArray().toByteString(),
            signature = "ServerSignature".toByteArray().toByteString(),
            defaultCheckInLengthInMinutes = 180
        )
    }

    @Test
    fun `SignedTraceLocation_toTraceLocation() should map permanent event correctly`() {
        TraceLocationData.signedTraceLocationPermanent.toTraceLocation() shouldBe TraceLocation(
            guid = "serverGeneratedGuid",
            version = 1,
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER,
            description = "IceCream Shop",
            address = "IceCream Wonderland Street 1",
            startDate = null,
            endDate = null,
            byteRepresentation = TraceLocationData.signedTraceLocationPermanent.location.toByteArray().toByteString(),
            signature = "ServerSignature".toByteArray().toByteString(),
            defaultCheckInLengthInMinutes = 30
        )
    }
}
