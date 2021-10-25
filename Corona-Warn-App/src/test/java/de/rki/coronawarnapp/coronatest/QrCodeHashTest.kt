package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class QrCodeHashTest {

    @Test
    fun qrCodeHashing() {
        val pcrQrCode = "https://localhost/?123456-12345678-1234-4da7-b166-b86d85475064"
        val ratQrCode =
            "https://s.coronawarn.app/?v=1#eyJ0aW1lc3RhbXAiOjE2MTg1NjA5NjQsInNhbHQiOiIwQ0ZEMUJCQ" +
                "zI2Q0FCODVCNkZFNDE5MTJFQjFBQUU1QUNEN0QyNjA0RTQwNTQyRUVEQjZEQUYyQkRBMDQ5QzRGIiwi" +
                "dGVzdElkIjoiYjM2YzUzN2ItZWQ5NC00Njc3LTkzZmQtODUwMTY4NjlkYjEwIiwiaGFzaCI6IjJiNTc" +
                "0NjhlN2Q4MTkyMWQzOGM4OGI1NjExOWE0Y2ViMzYyNmI1MDM4ZWI5Njk3ZjkxOTQ4NmJjMzg0Y2U2M2UifQ"

        ratQrCode.toSHA256() shouldBe "0c3bb39345b8e8c678e73162ce547aa1bbbb2526ee70a2efcfa3460f31040687"
        pcrQrCode.toSHA256() shouldBe "ad5fee4b8929fc1f3d9337736ba79053545c2327701a3349ec06ef59a3b1a57f"
    }
}
