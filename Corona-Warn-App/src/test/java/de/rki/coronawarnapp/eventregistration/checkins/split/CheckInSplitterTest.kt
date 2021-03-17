package de.rki.coronawarnapp.eventregistration.checkins.split

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import io.kotest.matchers.shouldBe
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
        version = 1,
        type = 1,
        description = "Restaurant",
        address = "Around the corner",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        signature = "signature",
        checkInStart = Instant.now(),
        checkInEnd = Instant.now(),
        targetCheckInEnd = null,
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
           Example 2 - no-checkout-time check-in
           localCheckIn = { start: '2021-03-04 09:30', end: null }
           splitInto = [{ start: '2021-03-04 09:30', end: null } // no split
        */
        val checkIn = defaultCheckIn.copy(
            checkInStart = Instant.parse("2021-03-04T09:30:00Z"),
            checkInEnd = null
        )

        checkIn.splitByMidnightUTC().apply {
            size shouldBe 1 // No splitting
            get(0) shouldBe checkIn
        }
    }

    @Test
    fun `Scenario 3`() {
        /*
           Example 3 - same-start-end-time check-in
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
    fun `Scenario 4`() {
        /*
        // Example 2 - 2-days check-in
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
    fun `Scenario 5`() {
        /*
         // Example 3 - 3-day check-in
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
}
