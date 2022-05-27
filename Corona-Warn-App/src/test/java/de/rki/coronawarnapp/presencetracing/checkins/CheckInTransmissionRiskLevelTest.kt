package de.rki.coronawarnapp.presencetracing.checkins

import de.rki.coronawarnapp.submission.task.TransmissionRiskVector
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.encode
import java.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CheckInTransmissionRiskLevelTest : BaseTest() {

    private val checkIn = CheckIn(
        id = 1L,
        traceLocationId = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode(),
        version = 1,
        type = 2,
        description = "restaurant_1",
        address = "address_1",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.parse("2021-03-04T10:20:00Z"),
        checkInEnd = Instant.parse("2021-03-04T10:30:00Z"),
        completed = false,
        createJournalEntry = false
    )

    private val transmissionVector = TransmissionRiskVector(
        intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    )

    @Test
    fun `1 day age`() {
        checkIn.determineRiskTransmission(
            Instant.parse("2021-03-05T00:00:00Z"),
            transmissionVector
        ) shouldBe 2
    }

    @Test
    fun `8 days age`() {
        checkIn.determineRiskTransmission(
            Instant.parse("2021-03-12T00:00:00Z"),
            transmissionVector
        ) shouldBe 9
    }

    @Test
    fun `age does not exist in transmission vector`() {
        checkIn.determineRiskTransmission(
            Instant.parse("2021-03-25T00:00:00Z"),
            transmissionVector
        ) shouldBe 1
    }

    @Test
    fun `start and now times are the same`() {
        checkIn.determineRiskTransmission(
            Instant.parse("2021-03-04T10:20:00Z"),
            transmissionVector
        ) shouldBe 1
    }

    @Test
    fun `negative age`() {
        checkIn.determineRiskTransmission(
            Instant.parse("2021-03-01T10:20:00Z"),
            transmissionVector
        ) shouldBe 1
    }
}
