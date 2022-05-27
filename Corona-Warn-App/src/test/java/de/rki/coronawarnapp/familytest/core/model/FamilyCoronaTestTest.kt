package de.rki.coronawarnapp.familytest.core.model

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import io.kotest.matchers.shouldBe
import java.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class FamilyCoronaTestTest : BaseTest() {

    private val timestamp = Instant.parse("2021-03-20T06:00:00.000Z")

    val test = FamilyCoronaTest(
        personName = "Maria",
        coronaTest = CoronaTest(
            identifier = "familyTest1",
            type = BaseCoronaTest.Type.PCR,
            registeredAt = timestamp,
            registrationToken = "registrationToken1"
        )
    )

    @Test
    fun `update test result`() {
        val updated = test.updateTestResult(
            CoronaTestResult.PCR_NEGATIVE
        )
        updated.testResult shouldBe CoronaTestResult.PCR_NEGATIVE
        updated.coronaTest.hasResultChangeBadge shouldBe true
    }

    @Test
    fun `update test result without badge`() {
        val updated = test.updateTestResult(
            CoronaTestResult.PCR_OR_RAT_REDEEMED
        )
        updated.testResult shouldBe CoronaTestResult.PCR_OR_RAT_REDEEMED
        updated.coronaTest.hasResultChangeBadge shouldBe false
    }
}
