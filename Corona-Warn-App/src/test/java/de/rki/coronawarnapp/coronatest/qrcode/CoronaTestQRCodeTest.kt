package de.rki.coronawarnapp.coronatest.qrcode

import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestQRCodeTest : BaseTest() {

    private val instancePCR = CoronaTestQRCode.PCR("pcr")
    private val instanceRA = CoronaTestQRCode.RapidAntigen(hash = "ra", createdAt = Instant.EPOCH)

    @Test
    fun `PCR defaults`() {
        instancePCR.apply {
            isDccSupportedByPoc shouldBe true
            isDccConsentGiven shouldBe false
            dateOfBirth shouldBe null
        }
    }

    @Test
    fun `RA defaults`() {
        instanceRA.apply {
            isDccSupportedByPoc shouldBe false
            isDccConsentGiven shouldBe false
            dateOfBirth shouldBe null
        }
    }
}
