package de.rki.coronawarnapp.familytest.core.model

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestTest : BaseTest() {

    val test = FamilyCoronaTest(
        personName = "Chilja",
        coronaTest = CoronaTest(
            identifier = "familyTest1",
            type = BaseCoronaTest.Type.PCR,
            registeredAt = Instant.parse("2021-03-20T06:00:00.000Z"),
            registrationToken = "registrationToken1"
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `update test result`() {
        val updated = test.copy(
            coronaTest = test.coronaTest.updateTestResult(
                CoronaTestResult.PCR_NEGATIVE
            )
        )

        updated.coronaTest.testResult shouldBe CoronaTestResult.PCR_NEGATIVE
    }
}
