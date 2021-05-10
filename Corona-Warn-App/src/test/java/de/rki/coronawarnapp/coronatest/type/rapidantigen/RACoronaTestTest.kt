package de.rki.coronawarnapp.coronatest.type.rapidantigen

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RACoronaTestTest : BaseTest() {

    @Test
    fun `a test is final if it reaches the REDEEMED state`() {
        val instance = RACoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = Instant.EPOCH,
            registrationToken = "token",
            testResult = CoronaTestResult.RAT_REDEEMED,
            testedAt = Instant.EPOCH,
        )

        instance.isFinal shouldBe true
        instance.copy(testResult = CoronaTestResult.RAT_POSITIVE).isFinal shouldBe false
        instance.copy(testResult = CoronaTestResult.PCR_REDEEMED).isFinal shouldBe false
    }
}
