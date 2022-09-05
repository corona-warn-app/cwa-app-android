package de.rki.coronawarnapp.presencetracing.checkins.common

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.toUserTimeZone
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encode
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class CheckInExtensionTest : BaseTest() {

    private val testCheckIn = CheckIn(
        id = 42L,
        traceLocationId = "traceLocationId1".decodeBase64()!!,
        version = 1,
        type = 1,
        description = "Restaurant",
        address = "Around the corner",
        traceLocationStart = Instant.parse("2021-03-04T22:00:00Z"),
        traceLocationEnd = Instant.parse("2021-03-04T23:00:00Z"),
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.parse("2021-03-04T22:00:00Z"),
        checkInEnd = Instant.parse("2021-03-04T23:00:00Z"),
        completed = false,
        createJournalEntry = true
    )

    private fun Instant.toPrettyDate(): String =
        toUserTimeZone().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

    @Test
    fun `Location name concatenates description, address and if both are set trace location start and end date`() {
        val testCheckInNoTraceLocationStartDate = testCheckIn.copy(traceLocationStart = null)
        val testCheckInNoTraceLocationEndDate = testCheckIn.copy(traceLocationEnd = null)
        val testCheckInNoTraceLocationStartAndEndDate =
            testCheckIn.copy(traceLocationStart = null, traceLocationEnd = null)

        testCheckIn.validateLocationName()
        testCheckInNoTraceLocationStartDate.validateLocationName()
        testCheckInNoTraceLocationEndDate.validateLocationName()
        testCheckInNoTraceLocationStartAndEndDate.validateLocationName()
    }

    private fun CheckIn.validateLocationName() {
        locationName shouldBe when (traceLocationStart != null && traceLocationEnd != null) {
            true ->
                "$description, $address, ${traceLocationStart?.toPrettyDate()} - ${traceLocationEnd?.toPrettyDate()}"
            else -> "$description, $address"
        }
    }
}
