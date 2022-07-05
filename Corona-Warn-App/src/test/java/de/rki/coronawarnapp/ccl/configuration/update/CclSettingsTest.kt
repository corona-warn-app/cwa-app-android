package de.rki.coronawarnapp.ccl.configuration.update

import de.rki.coronawarnapp.ccl.dccadmission.scenariosJson
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore

internal class CclSettingsTest : BaseTest() {

    private val fakeDataStore = FakeDataStore()

    @Test
    fun `test CclSettings - set last execution value and clear it again`() = runTest(UnconfinedTestDispatcher()) {
        val cclSettings = CclSettings(fakeDataStore, this)
        cclSettings.getLastExecutionTime() shouldBe null

        val now = Instant.parse("2022-04-02T00:00:00.000Z")
        cclSettings.setExecutionTimeToNow(now)

        fakeDataStore[CclSettings.LAST_EXECUTION_TIME_KEY] shouldBe now.seconds
        cclSettings.getLastExecutionTime() shouldBe now

        cclSettings.reset()
        cclSettings.getLastExecutionTime() shouldBe null
    }

    @Test
    fun `test CclSettings - set admission scenario identifier`() = runTest(UnconfinedTestDispatcher()) {
        val cclSettings = CclSettings(fakeDataStore, this)

        cclSettings.admissionScenarioId() shouldBe ""

        cclSettings.saveAdmissionScenarioId("Ad-Sc-ID")

        fakeDataStore[CclSettings.ADMISSION_SCENARIO_ID_KEY] shouldBe "Ad-Sc-ID"
        cclSettings.admissionScenarioId() shouldBe "Ad-Sc-ID"

        cclSettings.reset()
        cclSettings.admissionScenarioId() shouldBe ""
    }

    @Test
    fun `test CclSettings - set admission check scenarios`() = runTest(UnconfinedTestDispatcher()) {
        val cclSettings = CclSettings(fakeDataStore, this)

        cclSettings.admissionCheckScenarios.first() shouldBe null

        cclSettings.setAdmissionCheckScenarios(scenariosJson)

        fakeDataStore[CclSettings.ADMISSION_CHECK_SCENARIOS_KEY] shouldBe scenariosJson
        cclSettings.admissionCheckScenarios.first() shouldBe scenariosJson

        cclSettings.reset()
        cclSettings.admissionCheckScenarios.first() shouldBe null
    }
}
