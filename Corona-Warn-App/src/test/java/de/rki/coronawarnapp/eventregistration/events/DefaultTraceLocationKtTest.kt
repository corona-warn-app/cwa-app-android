package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationEntity
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class DefaultTraceLocationKtTest : BaseTest() {

    @Test
    fun `toTraceLocation() should map to correct object when providing all arguments`() {
        TraceLocationEntity(
            guid = "TestGuid",
            version = 1,
            type = TraceLocation.Type.TEMPORARY_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
            endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 15,
            signature = "signature"
        ).toTraceLocation() shouldBe DefaultTraceLocation(
            guid = "TestGuid",
            version = 1,
            type = TraceLocation.Type.TEMPORARY_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
            endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 15,
            signature = "signature"
        )
    }

    @Test
    fun `toTraceLocation() should map to correct object when providing only arguments that are required`() {
        TraceLocationEntity(
            guid = "TestGuid",
            version = 1,
            type = TraceLocation.Type.PERMANENT_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = null,
            signature = "signature"
        ).toTraceLocation() shouldBe DefaultTraceLocation(
            guid = "TestGuid",
            version = 1,
            type = TraceLocation.Type.PERMANENT_OTHER,
            description = "TestTraceLocation",
            address = "TestTraceLocationAddress",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = null,
            signature = "signature"
        )
    }

    @Test
    fun `toTraceLocations() should map a list of TraceLocationEntities correctly`() {
        listOf(
            TraceLocationEntity(
                guid = "TestGuid1",
                version = 1,
                type = TraceLocation.Type.TEMPORARY_OTHER,
                description = "TestTraceLocation1",
                address = "TestTraceLocationAddress1",
                startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
                endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
                defaultCheckInLengthInMinutes = 15,
                signature = "signature"
            ),
            TraceLocationEntity(
                guid = "TestGuid2",
                version = 1,
                type = TraceLocation.Type.PERMANENT_OTHER,
                description = "TestTraceLocation2",
                address = "TestTraceLocationAddress2",
                startDate = null,
                endDate = null,
                defaultCheckInLengthInMinutes = null,
                signature = "signature"
            )
        ).toTraceLocations() shouldBe listOf(
            DefaultTraceLocation(
                guid = "TestGuid1",
                version = 1,
                type = TraceLocation.Type.TEMPORARY_OTHER,
                description = "TestTraceLocation1",
                address = "TestTraceLocationAddress1",
                startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
                endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
                defaultCheckInLengthInMinutes = 15,
                signature = "signature"
            ),
            DefaultTraceLocation(
                guid = "TestGuid2",
                version = 1,
                type = TraceLocation.Type.PERMANENT_OTHER,
                description = "TestTraceLocation2",
                address = "TestTraceLocationAddress2",
                startDate = null,
                endDate = null,
                defaultCheckInLengthInMinutes = null,
                signature = "signature"
            )
        )
    }
}