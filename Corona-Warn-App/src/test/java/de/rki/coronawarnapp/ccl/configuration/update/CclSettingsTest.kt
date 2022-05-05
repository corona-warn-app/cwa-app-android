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

        cclSettings.clear()
        cclSettings.getLastExecutionTime() shouldBe null
    }

    @Test
    fun `test CclSettings - set admission scenario identifier`() = runTest(UnconfinedTestDispatcher()) {
        val cclSettings = CclSettings(fakeDataStore, this)

        cclSettings.getAdmissionScenarioId() shouldBe ""

        cclSettings.setAdmissionScenarioId("Ad-Sc-ID")

        fakeDataStore[CclSettings.ADMISSION_SCENARIO_ID_KEY] shouldBe "Ad-Sc-ID"
        cclSettings.getAdmissionScenarioId() shouldBe "Ad-Sc-ID"

        cclSettings.clear()
        cclSettings.getAdmissionScenarioId() shouldBe ""
    }

    @Test
    fun `test CclSettings - set admission check scenarios`() = runTest(UnconfinedTestDispatcher()) {
        val cclSettings = CclSettings(fakeDataStore, this)

        cclSettings.admissionCheckScenarios.first() shouldBe null

        cclSettings.setAdmissionCheckScenarios(scenariosJson)

        fakeDataStore[CclSettings.ADMISSION_CHECK_SCENARIOS_KEY] shouldBe scenariosJson
        cclSettings.admissionCheckScenarios.first() shouldBe scenariosJson

        cclSettings.clear()
        cclSettings.admissionCheckScenarios.first() shouldBe null
    }

    @Test
    fun `test CclSettings - forceCclCalculation`() = runTest(UnconfinedTestDispatcher()) {
        val cclSettings = CclSettings(fakeDataStore, this)

        // Call - 1-> returns `true` and set value to `false`
        fakeDataStore[CclSettings.FORCE_CCL_CALCULATION_KEY] shouldBe null
        cclSettings.forceCclCalculation() shouldBe true
        fakeDataStore[CclSettings.FORCE_CCL_CALCULATION_KEY] shouldBe false

        // Subsequent calls - just return what is saved
        // Call - 2
        fakeDataStore[CclSettings.FORCE_CCL_CALCULATION_KEY] shouldBe false
        cclSettings.forceCclCalculation() shouldBe false
        fakeDataStore[CclSettings.FORCE_CCL_CALCULATION_KEY] shouldBe false
        // Call - 3
        fakeDataStore[CclSettings.FORCE_CCL_CALCULATION_KEY] shouldBe false
        cclSettings.forceCclCalculation() shouldBe false
        fakeDataStore[CclSettings.FORCE_CCL_CALCULATION_KEY] shouldBe false
        // Call - 4
        fakeDataStore[CclSettings.FORCE_CCL_CALCULATION_KEY] shouldBe false
        cclSettings.forceCclCalculation() shouldBe false
        fakeDataStore[CclSettings.FORCE_CCL_CALCULATION_KEY] shouldBe false
    }
}
