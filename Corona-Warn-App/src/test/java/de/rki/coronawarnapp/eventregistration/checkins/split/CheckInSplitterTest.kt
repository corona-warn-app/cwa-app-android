package de.rki.coronawarnapp.eventregistration.checkins.split

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

class CheckInSplitterTest : BaseTest() {

    @Test
    fun `same-day check-in`() {
        /*
           Example 1 - same-day check-in
           localCheckIn = { start: '2021-03-04 09:30', end: '2021-03-04 09:45' }
           splitInto = [{ start: '2021-03-04 09:30', end: '2021-03-04 09:45' } // no split
        */
        val checkIn = CheckIn(
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
            checkInStart = Instant.parse("2021-03-04 09:30"),
            checkInEnd = Instant.parse("2021-03-04 09:45"),
            targetCheckInEnd = null,
            createJournalEntry = false
        )

        checkIn.splitByMidnight().apply {
            size shouldBe 1 // No splitting
            get(0) shouldBe checkIn
        }
    }

    @Test
    fun `2-days check-in`() {
        /*
        // Example 2 - 2-day check-in
        localCheckIn = { start: '2021-03-04 09:30', end: '2021-03-05 09:45' }
        splitInto = [
            { start: '2021-03-04 09:30', end: '2021-03-05 00:00' },
            { start: '2021-03-05 00:00', end: '2021-03-05 09:45' }
        ]
        */

        val checkIn = CheckIn(
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
            checkInStart = Instant.parse("2021-03-04 09:30"),
            checkInEnd = Instant.parse("2021-03-05 09:45"),
            targetCheckInEnd = null,
            createJournalEntry = false
        )

        checkIn.splitByMidnight().apply {
            size shouldBe 2
            get(0).apply {

            }

            get(1).apply {

            }
        }
    }

    @Test
    fun `3-days check-in`() {
        /*
         // Example 3 - 3-day check-in
         localCheckIn = { start: '2021-03-04 09:30', end: '2021-03-06 09:45' }
         splitInto = [
            { start: '2021-03-04 09:30', end: '2021-03-05 00:00' },
            { start: '2021-03-05 00:00', end: '2021-03-06 00:00' }
            { start: '2021-03-06 00:00', end: '2021-03-06 09:45' }
          ]
         */
        val checkIn = CheckIn(
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
            checkInStart = Instant.parse("2021-03-04 09:30"),
            checkInEnd = Instant.parse("2021-03-05 09:45"),
            targetCheckInEnd = null,
            createJournalEntry = false
        )

        checkIn.splitByMidnight().apply {
            size shouldBe 2
            get(0).apply {

            }

            get(1).apply {

            }
        }
    }
}