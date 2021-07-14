package de.rki.coronawarnapp.coronatest.type.pcr

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PCRCoronaTestTest : BaseTest() {

    @Test
    fun `a test is final if it reaches the REDEEMED state`() {
        val instance = PCRCoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = Instant.EPOCH,
            registrationToken = "token",
            testResult = CoronaTestResult.PCR_OR_RAT_REDEEMED,
        )

        instance.isFinal shouldBe true
        instance.copy(testResult = CoronaTestResult.PCR_POSITIVE).isFinal shouldBe false
        instance.copy(testResult = CoronaTestResult.RAT_POSITIVE).isFinal shouldBe false
    }
}
