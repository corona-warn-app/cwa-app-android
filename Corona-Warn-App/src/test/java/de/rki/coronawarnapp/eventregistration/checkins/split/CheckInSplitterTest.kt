package de.rki.coronawarnapp.eventregistration.checkins.split

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.joda.time.Instant
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

/**
 * Test examples reference [https://github.com/corona-warn-app/cwa-app-tech-spec/blob/proposal/event-registration-mvp/
 * docs/spec/event-registration-client.md#split-checkin-by-midnight-utc]
 */
class CheckInSplitterTest : BaseTest() {

    private val defaultCheckIn = CheckIn(
        id = 1L,
        guid = "eventOne",
        guidHash = byteArrayOf(),
        version = 1,
        type = 1,
        description = "Restaurant",
        address = "Around the corner",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        traceLocationBytes = byteArrayOf(),
        signature = "c2lnbmF0dXJl".decodeBase64()!!,
        checkInStart = Instant.now(),
        checkInEnd = Instant.now(),
        completed = false,
        createJournalEntry = false
    )

    @Test
    fun `Scenario 1`() {
        /*
           Example 1 - same-day check-in
           localCheckIn = { start: '2021-03-04 09:30', end: '2021-03-04 09:45' }
           splitInto = [{ start: '2021-03-04 09:30', end: '2021-03-04 09:45' } // no split
        */
        val checkIn = defaultCheckIn.copy(
            checkInStart = Instant.parse("2021-03-04T09:30:00Z"),
            checkInEnd = Instant.parse("2021-03-04T09:45:00Z")
        )

        checkIn.splitByMidnightUTC().apply {
            size shouldBe 1 // No splitting
            get(0) shouldBe checkIn
        }
    }

    @Test
    fun `Scenario 2`() {
        /*
           Example 2 - same-start-end-times check-in
           localCheckIn = { start: '2021-03-04 09:30', end: '2021-03-04 09:30' }
           splitInto = [{ start: '2021-03-04 09:30', end: '2021-03-04 09:30' } // no split
        */
        val checkIn = defaultCheckIn.copy(
            checkInStart = Instant.parse("2021-03-04T09:30:00Z"),
            checkInEnd = Instant.parse("2021-03-04T09:30:00Z")
        )

        checkIn.splitByMidnightUTC().apply {
            size shouldBe 1 // No splitting
            get(0) shouldBe checkIn
        }
    }

    @Test
    fun `Scenario 3`() {
        /*
        // Example 3 - 2-days check-in
        localCheckIn = { start: '2021-03-04 09:30', end: '2021-03-05 09:45' }
        splitInto = [
            { start: '2021-03-04 09:30', end: '2021-03-05 00:00' },
            { start: '2021-03-05 00:00', end: '2021-03-05 09:45' }
        ]
        */

        val checkIn = defaultCheckIn.copy(
            checkInStart = Instant.parse("2021-03-04T09:30:00Z"),
            checkInEnd = Instant.parse("2021-03-05T09:45:00Z")
        )

        checkIn.splitByMidnightUTC().apply {
            size shouldBe 2
            get(0) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-04T09:30:00Z"),
                checkInEnd = Instant.parse("2021-03-05T00:00:00Z")
            )

            get(1) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-05T00:00:00Z"),
                checkInEnd = Instant.parse("2021-03-05T09:45:00Z")
            )
        }
    }

    @Test
    fun `Scenario 4`() {
        /*
         // Example 4 - 3-days check-in
         localCheckIn = { start: '2021-03-04 09:30', end: '2021-03-06 09:45' }
         splitInto = [
            { start: '2021-03-04 09:30', end: '2021-03-05 00:00' },
            { start: '2021-03-05 00:00', end: '2021-03-06 00:00' }
            { start: '2021-03-06 00:00', end: '2021-03-06 09:45' }
          ]
         */
        val checkIn = defaultCheckIn.copy(
            checkInStart = Instant.parse("2021-03-04T09:30:00Z"),
            checkInEnd = Instant.parse("2021-03-06T09:45:00Z"),
        )

        checkIn.splitByMidnightUTC().apply {
            size shouldBe 3
            get(0) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-04T09:30:00Z"),
                checkInEnd = Instant.parse("2021-03-05T00:00:00Z")
            )

            get(1) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-05T00:00:00Z"),
                checkInEnd = Instant.parse("2021-03-06T00:00:00Z")
            )

            get(2) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-06T00:00:00Z"),
                checkInEnd = Instant.parse("2021-03-06T09:45:00Z")
            )
        }
    }

    @Test
    fun `Scenario 5`() {
        /*
        // Example 5 - 2-days-different-months check-in
        localCheckIn = { start: '2021-02-28 09:30', end: '2021-03-01 12:45' }
        splitInto = [
            { start: '2021-02-28 09:30', end: '2021-03-01 00:00' },
            { start: '2021-03-01 00:00', end: '2021-03-01 12:45' }
        ]
        */

        val checkIn = defaultCheckIn.copy(
            checkInStart = Instant.parse("2021-02-28T09:30:00Z"),
            checkInEnd = Instant.parse("2021-03-01T12:45:00Z")
        )

        checkIn.splitByMidnightUTC().apply {
            size shouldBe 2
            get(0) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-02-28T09:30:00Z"),
                checkInEnd = Instant.parse("2021-03-01T00:00:00Z")
            )

            get(1) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-01T00:00:00Z"),
                checkInEnd = Instant.parse("2021-03-01T12:45:00Z")
            )
        }
    }

    @Test
    fun `Scenario 6`() {
        /*
        // Example 6 - 2-days-different-years check-in
        localCheckIn = { start: '2021-12-31 09:30', end: '2022-01-01 12:45' }
        splitInto = [
            { start: '2021-12-31 09:30', end: '2022-01-01 00:00' },
            { start: '2022-01-01 00:00', end: '2022-01-01 12:45' }
        ]
        */

        val checkIn = defaultCheckIn.copy(
            checkInStart = Instant.parse("2021-12-31T09:30:00Z"),
            checkInEnd = Instant.parse("2022-01-01T12:45:00Z")
        )

        checkIn.splitByMidnightUTC().apply {
            size shouldBe 2
            get(0) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-12-31T09:30:00Z"),
                checkInEnd = Instant.parse("2022-01-01T00:00:00Z")
            )

            get(1) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2022-01-01T00:00:00Z"),
                checkInEnd = Instant.parse("2022-01-01T12:45:00Z")
            )
        }
    }

    @Test
    fun `Scenario 7`() {
        /*
        // Example 7 - 3-days-different-months-leap-year check-in
        localCheckIn = { start: '2020-02-28 09:30', end: '2020-03-01 12:45' }
        splitInto = [
            { start: '2020-02-28 09:30', end: '2020-02-29 00:00' },
            { start: '2020-02-29 00:00', end: '2020-03-01 00:00' },
            { start: '2020-03-01 00:00', end: '2020-03-01 12:45' }
        ]
        */

        val checkIn = defaultCheckIn.copy(
            checkInStart = Instant.parse("2020-02-28T09:30:00Z"),
            checkInEnd = Instant.parse("2020-03-01T12:45:00Z")
        )

        checkIn.splitByMidnightUTC().apply {
            size shouldBe 3
            get(0) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2020-02-28T09:30:00Z"),
                checkInEnd = Instant.parse("2020-02-29T00:00:00Z")
            )

            get(1) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2020-02-29T00:00:00Z"),
                checkInEnd = Instant.parse("2020-03-01T00:00:00Z")
            )

            get(2) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2020-03-01T00:00:00Z"),
                checkInEnd = Instant.parse("2020-03-01T12:45:00Z")
            )
        }
    }

    @Test
    fun `Scenario 8`() {
        /*
        // Example 8 - 2-dates duration < day check-in
        localCheckIn = { start: '2021-03-04 09:30', end: '2021-03-05 09:15' }
        splitInto = [
            { start: '2021-03-04 09:30', end: '2021-03-05 00:00' },
            { start: '2021-03-05 00:00', end: '2021-03-05 09:15' }
        ]
        */

        val checkIn = defaultCheckIn.copy(
            checkInStart = Instant.parse("2021-03-04T09:30:00Z"),
            checkInEnd = Instant.parse("2021-03-05T09:15:00Z")
        )

        checkIn.splitByMidnightUTC().apply {
            size shouldBe 2
            get(0) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-04T09:30:00Z"),
                checkInEnd = Instant.parse("2021-03-05T00:00:00Z")
            )

            get(1) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-05T00:00:00Z"),
                checkInEnd = Instant.parse("2021-03-05T09:15:00Z")
            )
        }
    }

    @Test
    fun `Scenario 9`() {
        /*
        // Example 9 - 2-dates duration < day - same start and end times check-in
        localCheckIn = { start: '2021-03-04 09:00', end: '2021-03-05 09:00' }
        splitInto = [
            { start: '2021-03-04 09:00', end: '2021-03-05 00:00' },
            { start: '2021-03-05 00:00', end: '2021-03-05 09:00' }
        ]
        */

        val checkIn = defaultCheckIn.copy(
            checkInStart = Instant.parse("2021-03-04T09:00:00Z"),
            checkInEnd = Instant.parse("2021-03-05T09:00:00Z")
        )

        checkIn.splitByMidnightUTC().apply {
            size shouldBe 2
            get(0) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-04T09:00:00Z"),
                checkInEnd = Instant.parse("2021-03-05T00:00:00Z")
            )

            get(1) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-05T00:00:00Z"),
                checkInEnd = Instant.parse("2021-03-05T09:00:00Z")
            )
        }
    }

    @Test
    fun `Scenario 10`() {
        /*
        // Example 10 - midnight times check-in
        localCheckIn = { start: '2021-03-04 00:00', end: '2021-03-05 00:00' }
        splitInto = [
            { start: '2021-03-04 00:00', end: '2021-03-05 00:00' } // No split
        ]
        */

        val checkIn = defaultCheckIn.copy(
            checkInStart = Instant.parse("2021-03-04T00:00:00Z"),
            checkInEnd = Instant.parse("2021-03-05T00:00:00Z")
        )

        checkIn.splitByMidnightUTC().apply {
            size shouldBe 1
            get(0) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-04T00:00:00Z"),
                checkInEnd = Instant.parse("2021-03-05T00:00:00Z")
            )
        }
    }

    @Test
    fun `Scenario 11`() {
        /*
        // Example 11 - 2min-2dates check-in
        localCheckIn = { start: '2021-03-04 23:59', end: '2021-03-05 00:01' }
        splitInto = [
            { start: '2021-03-04 00:00', end: '2021-03-05 00:00' } // No split
        ]
        */

        val checkIn = defaultCheckIn.copy(
            checkInStart = Instant.parse("2021-03-04T00:23:59Z"),
            checkInEnd = Instant.parse("2021-03-05T00:00:01Z")
        )

        checkIn.splitByMidnightUTC().apply {
            size shouldBe 2
            get(0) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-04T00:23:59Z"),
                checkInEnd = Instant.parse("2021-03-05T00:00:00Z")
            )

            get(1) shouldBe checkIn.copy(
                checkInStart = Instant.parse("2021-03-05T00:00:00.000Z"),
                checkInEnd = Instant.parse("2021-03-05T00:00:01Z")
            )
        }
    }
}
