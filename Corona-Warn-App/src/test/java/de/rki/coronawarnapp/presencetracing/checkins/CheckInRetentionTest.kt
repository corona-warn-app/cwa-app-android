package de.rki.coronawarnapp.presencetracing.checkins

import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.encode
import java.time.Instant
import org.junit.jupiter.api.Test

internal class CheckInRetentionTest {

    @Test
    fun `isWithinRetention() and isOutOfRetention() should return correct result`() {

        // Now = Jan 16th 2020, 00:00
        // CheckIns should be kept for 15 days, so every check-in with an end date before
        // Jan 1st 2020 is out of retention
        val now = Instant.parse("2020-01-16T00:00:00.000Z")

        val checkInWithinRetention = createCheckIn(Instant.parse("2020-01-01T00:00:00.000Z"))
        val checkInOutOfRetention = createCheckIn(Instant.parse("2019-12-31T23:59:59.000Z"))

        checkInWithinRetention.isWithinRetention(now) shouldBe true
        checkInWithinRetention.isOutOfRetention(now) shouldBe false

        checkInOutOfRetention.isWithinRetention(now) shouldBe false
        checkInOutOfRetention.isOutOfRetention(now) shouldBe true
    }

    private fun createCheckIn(checkOutDate: Instant) = CheckIn(
        traceLocationId = "traceLocationId1".encode(),
        version = 1,
        type = 1,
        description = "",
        address = "",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = 30,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.parse("1970-01-01T00:00:00.000Z"),
        checkInEnd = checkOutDate,
        completed = true,
        createJournalEntry = true
    )
}
