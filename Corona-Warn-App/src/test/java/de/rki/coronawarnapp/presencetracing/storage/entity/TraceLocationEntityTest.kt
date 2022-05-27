package de.rki.coronawarnapp.presencetracing.storage.entity

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.encode
import java.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class TraceLocationEntityTest : BaseTest() {

    @Test
    fun `toTraceLocationEntity() should map to TraceLocationEntity correctly with all arguments`() {
        TraceLocation(
            id = 1,
            version = 1,
            type = LOCATION_TYPE_TEMPORARY_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
            endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 15,
            cryptographicSeed = "seed byte array".encode(),
            cnPublicKey = "cnPublicKey"
        ).toTraceLocationEntity() shouldBe TraceLocationEntity(
            id = 1,
            version = 1,
            type = LOCATION_TYPE_TEMPORARY_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
            endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 15,
            cryptographicSeedBase64 = "seed byte array".encode().base64(),
            cnPublicKey = "cnPublicKey"
        )
    }

    @Test
    fun `toTraceLocationEntity() should map to TraceLocationEntity correctly with some arguments as null`() {
        TraceLocation(
            id = 1,
            version = 1,
            type = LOCATION_TYPE_PERMANENT_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = null,
            cryptographicSeed = "seed byte array".encode(),
            cnPublicKey = "cnPublicKey"
        ).toTraceLocationEntity() shouldBe TraceLocationEntity(
            id = 1,
            version = 1,
            type = LOCATION_TYPE_PERMANENT_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = null,
            cryptographicSeedBase64 = "seed byte array".encode().base64(),
            cnPublicKey = "cnPublicKey"
        )
    }
}
