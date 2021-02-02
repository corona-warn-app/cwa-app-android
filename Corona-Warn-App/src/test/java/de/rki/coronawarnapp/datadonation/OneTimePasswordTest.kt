package de.rki.coronawarnapp.datadonation

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.util.UUID

class OneTimePasswordTest {

    @Test
    fun test() {
        OneTimePassword(UUID.fromString("923c977a-617b-11eb-ae93-0242ac130002"))
            .payloadForRequest shouldBe
            byteArrayOf(
                57,
                50,
                51,
                99,
                57,
                55,
                55,
                97,
                45,
                54,
                49,
                55,
                98,
                45,
                49,
                49,
                101,
                98,
                45,
                97,
                101,
                57,
                51,
                45,
                48,
                50,
                52,
                50,
                97,
                99,
                49,
                51,
                48,
                48,
                48,
                50
            )
    }
}
