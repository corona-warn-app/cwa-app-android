package de.rki.coronawarnapp.coronatest.type.rapidantigen

import de.rki.coronawarnapp.appconfig.CoronaTestConfigContainer
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Instant
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
            testResult = CoronaTestResult.PCR_OR_RAT_REDEEMED,
            testedAt = Instant.EPOCH,
        )

        instance.isRedeemed shouldBe true
        instance.copy(testResult = CoronaTestResult.RAT_POSITIVE).isRedeemed shouldBe false
        instance.copy(testResult = CoronaTestResult.PCR_OR_RAT_REDEEMED).isRedeemed shouldBe true
    }

    @Test
    fun `Recycled test state is RECYCLED`() {
        val instance = RACoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = Instant.EPOCH,
            registrationToken = "token",
            testResult = CoronaTestResult.PCR_OR_RAT_REDEEMED,
            testedAt = Instant.EPOCH,
        )
        val testConfig = CoronaTestConfigContainer()

        instance.getState(Instant.EPOCH, testConfig) shouldNotBe RACoronaTest.State.RECYCLED

        instance.recycledAt = Instant.EPOCH
        instance.getState(Instant.EPOCH, testConfig) shouldBe RACoronaTest.State.RECYCLED
    }
}
