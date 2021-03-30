package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.eventregistration.events.TraceLocationUserInput
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.SecureRandom
import kotlin.random.Random

internal class TraceLocationUserInputToTraceLocationMapperTest {

    @MockK private lateinit var secureRandom: SecureRandom

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun toTraceLocation() {

        every { secureRandom.nextBytes(any()) } answers {
            val byteArray = arg<ByteArray>(0)
            Random(0).nextBytes(byteArray)
        }

        TraceLocationUserInput(
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT,
            description = "Top Secret Private Event",
            address = "top secret address",
            startDate = Instant.parse("2020-01-01T14:00:00.000Z"),
            endDate = Instant.parse("2020-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 180
        ).toTraceLocation(secureRandom) shouldBe TraceLocation(
            id = 0,
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT,
            description = "Top Secret Private Event",
            address = "top secret address",
            startDate = Instant.parse("2020-01-01T14:00:00.000Z"),
            endDate = Instant.parse("2020-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 180,
            cryptographicSeed = "2cc2b48c50aefe53b3974ed91e6b4ea9".decodeHex().toByteArray().toByteString(),
            cnPublicKey = "hardcoded public key TODO: replace with real one"
        )
    }
}
