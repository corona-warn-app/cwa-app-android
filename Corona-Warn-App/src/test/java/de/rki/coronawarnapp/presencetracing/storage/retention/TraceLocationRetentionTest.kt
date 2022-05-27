package de.rki.coronawarnapp.presencetracing.storage.retention

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.encode
import java.time.Instant
import org.junit.jupiter.api.Test

internal class TraceLocationRetentionTest {

    @Test
    fun `isWithinRetention() and isOutOfRetention() should return correct result`() {

        // Now = Jan 16th 2020, 00:00
        // TraceLocations should be kept for 15 days, so every trace location with and end date before
        // Jan 1st 2020, 00:00 should be out of retention
        val now = Instant.parse("2020-01-16T00:00:00.000Z")

        val traceLocationWithinRetention = createTraceLocation(Instant.parse("2020-01-01T00:00:00.000Z"))
        val traceLocationOutOfRetention = createTraceLocation(Instant.parse("2019-12-31T23:59:59.000Z"))
        val traceLocationNoEndDate = createTraceLocation(null)

        traceLocationWithinRetention.isWithinRetention(now) shouldBe true
        traceLocationWithinRetention.isOutOfRetention(now) shouldBe false

        traceLocationOutOfRetention.isWithinRetention(now) shouldBe false
        traceLocationOutOfRetention.isOutOfRetention(now) shouldBe true

        // trace locations without end date are never out of retention
        traceLocationNoEndDate.isWithinRetention(now) shouldBe true
        traceLocationNoEndDate.isOutOfRetention(now) shouldBe false
    }

    private fun createTraceLocation(endDate: Instant?) = TraceLocation(
        id = 1,
        type = TraceLocationOuterClass.TraceLocationType.UNRECOGNIZED,
        description = "",
        address = "",
        startDate = null,
        endDate = endDate,
        defaultCheckInLengthInMinutes = 30,
        cryptographicSeed = "seed byte array".encode(),
        cnPublicKey = "cnPublicKey"
    )
}
