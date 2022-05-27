package de.rki.coronawarnapp.presencetracing.locations

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.getDefaultAutoCheckoutLengthInMinutes
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.encode
import java.time.Instant
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
            .getDefaultAutoCheckoutLengthInMinutes() shouldBe expectedDefaultAutoCheckoutLength
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
                defaultCheckInLengthInMinutes = 30,
                startDate = null,
                endDate = null,
                expectedDefaultAutoCheckoutLength = 30,
            ),
            DefaultAutoCheckoutLengthTestCase(
                // min valid length = 00:15h
                defaultCheckInLengthInMinutes = 0,
                startDate = null,
                endDate = null,
                expectedDefaultAutoCheckoutLength = MIN_VALID_LENGTH
            ),
            DefaultAutoCheckoutLengthTestCase(
                // TraceLocations with CWA can actually only have 15 minute interval lengths. However, a trace location
                // created by a third party could create arbitrary lengths.
                // 22 min should be rounded to 15
                defaultCheckInLengthInMinutes = 22,
                startDate = null,
                endDate = null,
                expectedDefaultAutoCheckoutLength = 15
            ),
            DefaultAutoCheckoutLengthTestCase(
                // TraceLocations with CWA can actually only have 15 minute interval lengths. However, a trace location
                // created by a third party could create arbitrary lengths.
                // 23 min should be rounded to 30
                defaultCheckInLengthInMinutes = 23,
                startDate = null,
                endDate = null,
                expectedDefaultAutoCheckoutLength = 30
            ),
            DefaultAutoCheckoutLengthTestCase(
                // max valid length = 23:45h
                defaultCheckInLengthInMinutes = MAX_VALID_LENGTH,
                startDate = null,
                endDate = null,
                expectedDefaultAutoCheckoutLength = MAX_VALID_LENGTH
            ),
            DefaultAutoCheckoutLengthTestCase(
                // max valid length = 23:45h
                // TraceLocations with CWA can actually only have a max length of 23:45h. However, a trace location
                // created by a third party could have a bigger length.
                defaultCheckInLengthInMinutes = MAX_VALID_LENGTH + 1,
                startDate = null,
                endDate = null,
                expectedDefaultAutoCheckoutLength = MAX_VALID_LENGTH
            ),
            DefaultAutoCheckoutLengthTestCase(
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-24T17:00:00.000Z"),
                // 2 hours duration
                expectedDefaultAutoCheckoutLength = 120
            ),
            DefaultAutoCheckoutLengthTestCase(
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:05:00.000Z"),
                endDate = Instant.parse("2021-12-24T17:00:00.000Z"),
                // 1 hour 55 minutes duration rounded to 2 hours
                expectedDefaultAutoCheckoutLength = 120
            ),
            DefaultAutoCheckoutLengthTestCase(
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-24T15:05:00.000Z"),
                // 5 minutes duration rounded to 15 minutes
                expectedDefaultAutoCheckoutLength = MIN_VALID_LENGTH
            ),
            DefaultAutoCheckoutLengthTestCase(
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-26T15:00:00.000Z"),
                // More than 1 day duration
                expectedDefaultAutoCheckoutLength = MAX_VALID_LENGTH
            ),
            DefaultAutoCheckoutLengthTestCase(
                defaultCheckInLengthInMinutes = null,
                startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
                endDate = Instant.parse("2021-12-24T15:50:00.000Z"),
                // 50 minutes duration rounded to 45
                expectedDefaultAutoCheckoutLength = 45
            )
        )
    }
}

data class DefaultAutoCheckoutLengthTestCase(
    val defaultCheckInLengthInMinutes: Int?,
    val startDate: Instant?,
    val endDate: Instant?,
    val expectedDefaultAutoCheckoutLength: Int,
)
