package de.rki.coronawarnapp.presencetracing.locations

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.getDefaultAutoCheckoutLengthInMinutes
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.encode
import org.joda.time.Instant
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import testhelpers.BaseTest
import java.util.concurrent.TimeUnit

class DefaultAutoCheckoutLengthTest : BaseTest() {

    @ParameterizedTest
    @MethodSource("provideArguments")
    fun `getDefaultAuthCheckoutLengthInMinutes(now) should return correct value`(
        testCase: DefaultAutoCheckoutLengthTestCase
    ) = with(testCase) {

        createTraceLocation(this)
            .getDefaultAutoCheckoutLengthInMinutes(now) shouldBe expectedDefaultAutoCheckoutLength
    }

    private fun createTraceLocation(testCase: DefaultAutoCheckoutLengthTestCase) = TraceLocation(
        id = 1,
        type = TraceLocationOuterClass.TraceLocationType.UNRECOGNIZED,
        description = "",
        address = "",
        startDate = testCase.startDate,
        endDate = testCase.endDate,
        defaultCheckInLengthInMinutes = testCase.defaultCheckInLengthInMinutes,
        cryptographicSeed = "seed byte array".encode(),
        cnPublicKey = "cnPublicKey"
    )

    companion object {

        // min valid length = 00:15h
        private const val MIN_VALID_LENGTH = 15

        // max valid length = 23:45h
        private val MAX_VALID_LENGTH = (TimeUnit.HOURS.toMinutes(23) + 45).toInt()

        @Suppress("unused")
        @JvmStatic
        fun provideArguments() = listOf(
            DefaultAutoCheckoutLengthTestCase(
                // now doesn't matter, as defaultCheckInLengthInMinutes is not null
                now = Instant.parse("1970-01-01T00:00:00.000Z"),
                defaultCheckInLengthInMinutes = 30,
                startDate = null,
                endDate = null,
                expectedDefaultAutoCheckoutLength = 30,
            ),
            DefaultAutoCheckoutLengthTestCase(
                // now doesn't matter here, as defaultCheckInLengthInMinutes is not null
                now = Instant.parse("1970-01-01T00:00:00.000Z"),
                // min valid length = 00:15h
                defaultCheckInLengthInMinutes = 0,
                startDate = null,
                endDate = null,
                expectedDefaultAutoCheckoutLength = MIN_VALID_LENGTH
            ),
            DefaultAutoCheckoutLengthTestCase(
                // now doesn't matter here, as defaultCheckInLengthInMinutes is not null
                now = Instant.parse("1970-01-01T00:00:00.000Z"),
                // TraceLocations with CWA can actually only have 15 minute interval lengths. However, a trace location
                // created by a third party could create arbitrary lengths.
                // 22 min should be rounded to 15
                defaultCheckInLengthInMinutes = 22,
                startDate = null,
                endDate = null,
                expectedDefaultAutoCheckoutLength = 15
            ),
            DefaultAutoCheckoutLengthTestCase(
                // now doesn't matter here, as defaultCheckInLengthInMinutes is not null
                now = Instant.parse("1970-01-01T00:00:00.000Z"),
                // TraceLocations with CWA can actually only have 15 minute interval lengths. However, a trace location
                // created by a third party could create arbitrary lengths.
                // 23 min should be rounded to 30
                defaultCheckInLengthInMinutes = 23,
                startDate = null,
                endDate = null,
                expectedDefaultAutoCheckoutLength = 30
            ),
            DefaultAutoCheckoutLengthTestCase(
                // now doesn't matter here, as defaultCheckInLengthInMinutes is not null
                now = Instant.parse("1970-01-01T00:00:00.000Z"),
                // max valid length = 23:45h
                defaultCheckInLengthInMinutes = MAX_VALID_LENGTH,
                startDate = null,
                endDate = null,
                expectedDefaultAutoCheckoutLength = MAX_VALID_LENGTH
            ),
            DefaultAutoCheckoutLengthTestCase(
                // now doesn't matter here, as defaultCheckInLengthInMinutes is not null
                now = Instant.parse("1970-01-01T00:00:00.000Z"),
                // max valid length = 23:45h
                // TraceLocations with CWA can actually only have a max length of 23:45h. However, a trace location
                // created by a third party could have a bigger length.
                defaultCheckInLengthInMinutes = MAX_VALID_LENGTH + 1,
                startDate = null,
                endDate = null,
                expectedDefaultAutoCheckoutLength = MAX_VALID_LENGTH
            ),
            DefaultAutoCheckoutLengthTestCase(
                now = Instant.parse("2021-12-23T17:00:00.000Z"),
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-24T17:00:00.000Z"),
                // use 23:45h if user checks in earlier than 23:45 before the event ends
                expectedDefaultAutoCheckoutLength = MAX_VALID_LENGTH
            ),
            DefaultAutoCheckoutLengthTestCase(
                now = Instant.parse("2021-12-23T17:30:00.000Z"),
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-24T17:00:00.000Z"),
                // 23:30h in minutes
                expectedDefaultAutoCheckoutLength = (TimeUnit.HOURS.toMinutes(23) + 30).toInt()
            ),
            DefaultAutoCheckoutLengthTestCase(
                now = Instant.parse("2021-12-24T16:00:00.000Z"),
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-24T17:00:00.000Z"),
                expectedDefaultAutoCheckoutLength = 60
            ),
            DefaultAutoCheckoutLengthTestCase(
                now = Instant.parse("2021-12-24T17:01:00.000Z"),
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-24T17:00:00.000Z"),
                expectedDefaultAutoCheckoutLength = 15
            ),
            DefaultAutoCheckoutLengthTestCase(
                now = Instant.parse("2021-12-24T17:30:00.000Z"),
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-24T17:00:00.000Z"),
                // We take the min value when the user checks in after the event has already ended
                expectedDefaultAutoCheckoutLength = 15
            ),
            DefaultAutoCheckoutLengthTestCase(
                now = Instant.parse("2021-12-24T16:31:00.000Z"),
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-24T17:00:00.000Z"),
                // Event ends in 29min ->  we round to the nearest 15 minutes
                expectedDefaultAutoCheckoutLength = 30
            ),
            DefaultAutoCheckoutLengthTestCase(
                now = Instant.parse("2021-12-24T16:38:00.000Z"),
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-24T17:00:00.000Z"),
                // Event ends in 22min ->  we round to the nearest 15 minutes
                expectedDefaultAutoCheckoutLength = 15
            ),
            DefaultAutoCheckoutLengthTestCase(
                now = Instant.parse("2021-12-24T16:59:00.000Z"),
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-24T17:00:00.000Z"),
                // Event ends in 1min ->  we are not rounding to 0 but to the min value (15min)
                expectedDefaultAutoCheckoutLength = 15
            ),
            DefaultAutoCheckoutLengthTestCase(
                now = Instant.parse("2021-12-24T17:00:00.000Z"),
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-24T17:00:00.000Z"),
                // We check in at event end, return min value (15min)
                expectedDefaultAutoCheckoutLength = 15
            )
        )
    }
}

data class DefaultAutoCheckoutLengthTestCase(
    val now: Instant,
    val defaultCheckInLengthInMinutes: Int?,
    val startDate: Instant?,
    val endDate: Instant?,
    val expectedDefaultAutoCheckoutLength: Int,
)
