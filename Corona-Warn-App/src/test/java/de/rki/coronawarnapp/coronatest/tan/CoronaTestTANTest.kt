package de.rki.coronawarnapp.coronatest.tan

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestTANTest : BaseTest() {

    private val instancePCR = CoronaTestTAN.PCR("tan")

    @Test
    fun `dcc is not supported by tans`() {
        instancePCR.apply {
            isDccConsentGiven shouldBe false
            isDccSupportedByPoc shouldBe false
            dateOfBirth shouldBe null
        }
    }
}
