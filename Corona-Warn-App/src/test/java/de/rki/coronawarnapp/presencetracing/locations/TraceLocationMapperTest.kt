package de.rki.coronawarnapp.presencetracing.locations

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.toTraceLocation
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.toTraceLocations
import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationEntity
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.encode
import java.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class TraceLocationMapperTest : BaseTest() {

    @Test
    fun `toTraceLocation() should map to correct object when providing all arguments`() {
        TraceLocationEntity(
            id = 1,
            version = 1,
            type = LOCATION_TYPE_PERMANENT_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
            endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 15,
            cryptographicSeedBase64 = "seed byte array".encode().base64(),
            cnPublicKey = "cnPublicKey"
        ).toTraceLocation() shouldBe TraceLocation(
            id = 1,
            version = 1,
            type = LOCATION_TYPE_PERMANENT_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
            endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 15,
            cryptographicSeed = "seed byte array".encode(),
            cnPublicKey = "cnPublicKey"
        )
    }

    @Test
    fun `toTraceLocation() should map to correct object when providing only arguments that are required`() {
        TraceLocationEntity(
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
        ).toTraceLocation() shouldBe TraceLocation(
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
        )
    }

    @Test
    fun `toTraceLocations() should map a list of TraceLocationEntities correctly`() {
        listOf(
            TraceLocationEntity(
                id = 1,
                version = 1,
                type = LOCATION_TYPE_TEMPORARY_OTHER,
                description = "TestTraceLocation1",
                address = "TestTraceLocationAddress1",
                startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
                endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
                defaultCheckInLengthInMinutes = 15,
                cryptographicSeedBase64 = "seed byte array".encode().base64(),
                cnPublicKey = "cnPublicKey"
            ),
            TraceLocationEntity(
                id = 1,
                version = 1,
                type = LOCATION_TYPE_PERMANENT_OTHER,
                description = "TestTraceLocation2",
                address = "TestTraceLocationAddress2",
                startDate = null,
                endDate = null,
                defaultCheckInLengthInMinutes = null,
                cryptographicSeedBase64 = "seed byte array".encode().base64(),
                cnPublicKey = "cnPublicKey"
            )
        ).toTraceLocations() shouldBe listOf(
            TraceLocation(
                id = 1,
                version = 1,
                type = LOCATION_TYPE_TEMPORARY_OTHER,
                description = "TestTraceLocation1",
                address = "TestTraceLocationAddress1",
                startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
                endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
                defaultCheckInLengthInMinutes = 15,
                cryptographicSeed = "seed byte array".encode(),
                cnPublicKey = "cnPublicKey"
            ),
            TraceLocation(
                id = 1,
                version = 1,
                type = LOCATION_TYPE_PERMANENT_OTHER,
                description = "TestTraceLocation2",
                address = "TestTraceLocationAddress2",
                startDate = null,
                endDate = null,
                defaultCheckInLengthInMinutes = null,
                cryptographicSeed = "seed byte array".encode(),
                cnPublicKey = "cnPublicKey"
            )
        )
    }
}
