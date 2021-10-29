package de.rki.coronawarnapp.reyclebin.coronatest.request

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RestoreRecycledTestRequestTest : BaseTest() {

    @Test
    fun toRestoreRecycledTestRequest() {
        PCRCoronaTest(
            identifier = "pcr-identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = Instant.EPOCH,
            registrationToken = "token",
            testResult = CoronaTestResult.PCR_NEGATIVE,
            isDccConsentGiven = true
        ).toRestoreRecycledTestRequest() shouldBe RestoreRecycledTestRequest(
            type = CoronaTest.Type.PCR,
            identifier = "pcr-identifier",
            isDccSupportedByPoc = true,
            isDccConsentGiven = true
        )

        RACoronaTest(
            identifier = "rat-identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = Instant.EPOCH,
            registrationToken = "token",
            testResult = CoronaTestResult.RAT_REDEEMED,
            testedAt = Instant.EPOCH,
            isDccConsentGiven = false,
            isDccSupportedByPoc = false
        ).toRestoreRecycledTestRequest() shouldBe RestoreRecycledTestRequest(
            type = CoronaTest.Type.RAPID_ANTIGEN,
            identifier = "rat-identifier",
            isDccSupportedByPoc = false,
            isDccConsentGiven = false
        )
    }
}
