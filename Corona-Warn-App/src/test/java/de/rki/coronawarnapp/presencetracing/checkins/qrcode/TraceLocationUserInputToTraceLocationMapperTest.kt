package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import de.rki.coronawarnapp.presencetracing.locations.TraceLocationUserInput
import de.rki.coronawarnapp.presencetracing.locations.toTraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import okio.ByteString.Companion.encode
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TraceLocationUserInputToTraceLocationMapperTest {

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun toTraceLocation() {
        TraceLocationUserInput(
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT,
            description = "Top Secret Private Event",
            address = "top secret address",
            startDate = Instant.parse("2020-01-01T14:00:00.000Z"),
            endDate = Instant.parse("2020-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 180
        ).toTraceLocation("cryptographicSeed".encode(), "cnPublicKey123") shouldBe TraceLocation(
            id = 0,
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT,
            description = "Top Secret Private Event",
            address = "top secret address",
            startDate = Instant.parse("2020-01-01T14:00:00.000Z"),
            endDate = Instant.parse("2020-01-01T18:00:00.000Z"),
            defaultCheckInLengthInMinutes = 180,
            cryptographicSeed = "cryptographicSeed".encode(),
            cnPublicKey = "cnPublicKey123"
        )
    }
}
