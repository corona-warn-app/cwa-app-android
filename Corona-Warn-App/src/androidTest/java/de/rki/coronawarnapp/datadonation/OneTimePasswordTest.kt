package de.rki.coronawarnapp.datadonation

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.util.UUID

class OneTimePasswordTest {

    @Test
    fun test() {
        OneTimePassword(UUID.fromString("923c977a-617b-11eb-ae93-0242ac130002"))
            .payloadForRequest shouldBe "kjyXemF7EeuukwJCrBMAAg==\n".encodeToByteArray()
    }
}
