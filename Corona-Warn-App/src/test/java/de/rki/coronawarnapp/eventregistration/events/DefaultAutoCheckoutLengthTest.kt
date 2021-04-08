package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.encode
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DefaultAutoCheckoutLengthTest : BaseTest() {

    @Test
    fun `should return defaultCheckInLengthInMinutes if it is not null`() {
        val now = Instant.parse("2021-12-24T16:00:00.000Z")
        createTraceLocation(30).getDefaultAutoCheckoutLengthInMinutes(now) shouldBe 30
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
