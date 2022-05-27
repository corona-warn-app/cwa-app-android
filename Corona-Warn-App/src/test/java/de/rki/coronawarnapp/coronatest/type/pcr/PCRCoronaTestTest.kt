package de.rki.coronawarnapp.coronatest.type.pcr

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Instant
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

        instance.isRedeemed shouldBe true
        instance.copy(testResult = CoronaTestResult.PCR_POSITIVE).isRedeemed shouldBe false
        instance.copy(testResult = CoronaTestResult.RAT_POSITIVE).isRedeemed shouldBe false
    }

    @Test
    fun `Recycled test state is RECYCLED`() {
        val instance = PCRCoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = Instant.EPOCH,
            registrationToken = "token",
            testResult = CoronaTestResult.PCR_OR_RAT_REDEEMED,
        )

        instance.state shouldNotBe PCRCoronaTest.State.RECYCLED

        instance.recycledAt = Instant.EPOCH
        instance.state shouldBe PCRCoronaTest.State.RECYCLED
    }
}
