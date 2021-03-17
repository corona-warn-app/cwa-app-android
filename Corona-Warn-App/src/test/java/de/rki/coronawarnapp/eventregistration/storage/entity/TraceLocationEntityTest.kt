package de.rki.coronawarnapp.eventregistration.storage.entity

import de.rki.coronawarnapp.eventregistration.events.DefaultTraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class TraceLocationEntityTest : BaseTest() {

    @Test
    fun `toTraceLocationEntity() should map to TraceLocationEntity correctly with all arguments`() {
        DefaultTraceLocation(
            guid = "TestGuid",
            version = 1,
            type = LOCATION_TYPE_TEMPORARY_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
            endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 15,
            signature = "signature".toByteArray().toByteString()
        ).toTraceLocationEntity() shouldBe TraceLocationEntity(
            guid = "TestGuid",
            version = 1,
            type = LOCATION_TYPE_TEMPORARY_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
            endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 15,
            signatureBase64 = "signature".toByteArray().toByteString().base64()
        )
    }

    @Test
    fun `toTraceLocationEntity() should map to TraceLocationEntity correctly with some arguments as null`() {
        DefaultTraceLocation(
            guid = "TestGuid",
            version = 1,
            type = LOCATION_TYPE_PERMANENT_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = null,
            signature = "signature".toByteArray().toByteString()
        ).toTraceLocationEntity() shouldBe TraceLocationEntity(
            guid = "TestGuid",
            version = 1,
            type = LOCATION_TYPE_PERMANENT_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = null,
            signatureBase64 = "signature".toByteArray().toByteString().base64()
        )
    }
}
