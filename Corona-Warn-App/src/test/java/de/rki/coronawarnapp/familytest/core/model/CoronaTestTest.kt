package de.rki.coronawarnapp.familytest.core.model

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import io.kotest.matchers.shouldBe
import java.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestTest : BaseTest() {

    private val timestamp = Instant.parse("2021-03-20T06:00:00.000Z")

    val test = CoronaTest(
        identifier = "test1",
        type = BaseCoronaTest.Type.PCR,
        registeredAt = timestamp,
        registrationToken = "registrationToken1"
    )

    @Test
    fun `update test result`() {
        val updated = test.updateTestResult(
            CoronaTestResult.PCR_NEGATIVE
        )
        updated.testResult shouldBe CoronaTestResult.PCR_NEGATIVE
    }

    @Test
    fun `move to recycle bin`() {
        val updated = test.moveToRecycleBin(timestamp)
        updated.recycledAt shouldBe timestamp
    }

    @Test
    fun `restore test`() {
        val updated = test.restore()
        updated.recycledAt shouldBe null
    }

    @Test
    fun `mark dcc created`() {
        val updated = test.markDccCreated(true)
        updated.isDccDataSetCreated shouldBe true
    }

    @Test
    fun `mark badge as viewed`() {
        val updated = test.markBadgeAsViewed()
        updated.didShowBadge shouldBe true
    }

    @Test
    fun `set lab id`() {
        val updated = test.updateLabId("lab1")
        updated.labId shouldBe "lab1"
    }

    @Test
    fun `set ResultNotification`() {
        val updated = test.updateResultNotification(true)
        updated.isResultAvailableNotificationSent shouldBe true
    }

    @Test
    fun `set SampleCollectedAt`() {
        val updated = test.updateSampleCollectedAt(timestamp)
        updated.additionalInfo?.sampleCollectedAt shouldBe timestamp
    }
}
