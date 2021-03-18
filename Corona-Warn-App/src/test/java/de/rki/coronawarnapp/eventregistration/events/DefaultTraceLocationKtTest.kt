package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.toTraceLocation
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.toTraceLocations
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationEntity
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class DefaultTraceLocationKtTest : BaseTest() {

    @Test
    fun `toTraceLocation() should map to correct object when providing all arguments`() {
        TraceLocationEntity(
            guid = "TestGuid",
            version = 1,
            type = LOCATION_TYPE_PERMANENT_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
            endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 15,
            byteRepresentationBase64 = "byteRepresentation".toByteArray().toByteString().base64(),
            signatureBase64 = "signature".toByteArray().toByteString().base64()
        ).toTraceLocation() shouldBe TraceLocation(
            guid = "TestGuid",
            version = 1,
            type = LOCATION_TYPE_PERMANENT_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
            endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 15,
            byteRepresentation = "byteRepresentation".toByteArray().toByteString(),
            signature = "signature".toByteArray().toByteString()
        )
    }

    @Test
    fun `toTraceLocation() should map to correct object when providing only arguments that are required`() {
        TraceLocationEntity(
            guid = "TestGuid",
            version = 1,
            type = LOCATION_TYPE_PERMANENT_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = null,
            byteRepresentationBase64 = "byteRepresentation".toByteArray().toByteString().base64(),
            signatureBase64 = "signature".toByteArray().toByteString().base64()
        ).toTraceLocation() shouldBe TraceLocation(
            guid = "TestGuid",
            version = 1,
            type = LOCATION_TYPE_PERMANENT_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = null,
            byteRepresentation = "byteRepresentation".toByteArray().toByteString(),
            signature = "signature".toByteArray().toByteString()
        )
    }

    @Test
    fun `toTraceLocations() should map a list of TraceLocationEntities correctly`() {
        listOf(
            TraceLocationEntity(
                guid = "TestGuid1",
                version = 1,
                type = LOCATION_TYPE_TEMPORARY_OTHER,
                description = "TestTraceLocation1",
                address = "TestTraceLocationAddress1",
                startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
                endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
                defaultCheckInLengthInMinutes = 15,
                byteRepresentationBase64 = "byteRepresentation".toByteArray().toByteString().base64(),
                signatureBase64 = "signature".toByteArray().toByteString().base64()
            ),
            TraceLocationEntity(
                guid = "TestGuid2",
                version = 1,
                type = LOCATION_TYPE_PERMANENT_OTHER,
                description = "TestTraceLocation2",
                address = "TestTraceLocationAddress2",
                startDate = null,
                endDate = null,
                defaultCheckInLengthInMinutes = null,
                byteRepresentationBase64 = "byteRepresentation".toByteArray().toByteString().base64(),
                signatureBase64 = "signature".toByteArray().toByteString().base64()
            )
        ).toTraceLocations() shouldBe listOf(
            TraceLocation(
                guid = "TestGuid1",
                version = 1,
                type = LOCATION_TYPE_TEMPORARY_OTHER,
                description = "TestTraceLocation1",
                address = "TestTraceLocationAddress1",
                startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
                endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
                defaultCheckInLengthInMinutes = 15,
                byteRepresentation = "byteRepresentation".toByteArray().toByteString(),
                signature = "signature".toByteArray().toByteString()
            ),
            TraceLocation(
                guid = "TestGuid2",
                version = 1,
                type = LOCATION_TYPE_PERMANENT_OTHER,
                description = "TestTraceLocation2",
                address = "TestTraceLocationAddress2",
                startDate = null,
                endDate = null,
                defaultCheckInLengthInMinutes = null,
                byteRepresentation = "byteRepresentation".toByteArray().toByteString(),
                signature = "signature".toByteArray().toByteString()
            )
        )
    }
}
