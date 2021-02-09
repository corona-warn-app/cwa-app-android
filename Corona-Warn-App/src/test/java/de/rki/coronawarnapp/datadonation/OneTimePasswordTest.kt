package de.rki.coronawarnapp.datadonation

import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.Test
import java.util.UUID

class OneTimePasswordTest {

    @Test
    fun `payload generation`() {
        val otpPayload =
            OneTimePassword(UUID.fromString("15cff19f-af26-41bc-94f2-c1a65075e894"))
                .payloadForRequest
        val expected = "MTVjZmYxOWYtYWYyNi00MWJjLTk0ZjItYzFhNjUwNzVlODk0".decodeBase64()!!.toByteArray()
        otpPayload shouldBe expected
    }
}
