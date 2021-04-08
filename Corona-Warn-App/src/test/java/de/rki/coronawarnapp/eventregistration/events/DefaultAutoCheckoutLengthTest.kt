package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.encode
import org.joda.time.Instant
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

class DefaultAutoCheckoutLengthTest : BaseTest() {

    @ParameterizedTest
    @ArgumentsSource(DefaultAutoCheckoutLengthTestSetProvider::class)
    fun `getDefaultAuthCheckoutLengthInMinutes(now) should return correct value`(
        testSet: DefaultAutoCheckoutLengthTestSet
    ) = with(testSet) {

        createTraceLocation(defaultCheckInLengthInMinutes)
            .getDefaultAutoCheckoutLengthInMinutes(now) shouldBe expectedDefaultAutoCheckoutLength
    }

    private fun createTraceLocation(defaultCheckInLengthInMinutes: Int = 0) = TraceLocation(
        id = 1,
        type = TraceLocationOuterClass.TraceLocationType.UNRECOGNIZED,
        description = "",
        address = "",
        startDate = Instant.parse("2021-12-24T15:00:00.000Z"),
        endDate = Instant.parse("2021-12-24T17:00:00.000Z"),
        defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
        cryptographicSeed = "seed byte array".encode(),
        cnPublicKey = "cnPublicKey"
    )
}

data class DefaultAutoCheckoutLengthTestSet(
    val now: Instant,
    val defaultCheckInLengthInMinutes: Int,
    val expectedDefaultAutoCheckoutLength: Int
)

class DefaultAutoCheckoutLengthTestSetProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(
                DefaultAutoCheckoutLengthTestSet(
                    now = Instant.parse("2021-12-24T15:00:00.000Z"),
                    defaultCheckInLengthInMinutes = 30,
                    expectedDefaultAutoCheckoutLength = 30
                )
            ),
            Arguments.of(
                DefaultAutoCheckoutLengthTestSet(
                    now = Instant.parse("2021-12-24T15:00:00.000Z"),
                    // min valid length = 00:15h
                    defaultCheckInLengthInMinutes = 0,
                    expectedDefaultAutoCheckoutLength = MIN_VALID_LENGTH
                )
            ),
            Arguments.of(
                DefaultAutoCheckoutLengthTestSet(
                    now = Instant.parse("2021-12-24T15:00:00.000Z"),
                    // max valid length = 23:45h
                    defaultCheckInLengthInMinutes = MAX_VALID_LENGTH,
                    expectedDefaultAutoCheckoutLength = MAX_VALID_LENGTH
                )
            ),
            Arguments.of(
                DefaultAutoCheckoutLengthTestSet(
                    now = Instant.parse("2021-12-24T15:00:00.000Z"),
                    // max valid length = 23:45h
                    defaultCheckInLengthInMinutes = MAX_VALID_LENGTH + 1,
                    expectedDefaultAutoCheckoutLength = MAX_VALID_LENGTH
                )
            )
        )
    }

    companion object {

        // min valid length = 00:15h
        const val MIN_VALID_LENGTH = 15

        // max valid length = 23:45h
        val MAX_VALID_LENGTH = (TimeUnit.HOURS.toMinutes(23) + 45).toInt()
    }
}
