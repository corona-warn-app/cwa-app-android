package de.rki.coronawarnapp.ccl.configuration.update

import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore

internal class CCLSettingsTest : BaseTest() {

    private val fakeDataStore = FakeDataStore()
    private lateinit var cclSettings: CCLSettings

    @BeforeEach
    fun setup() {
        cclSettings = CCLSettings(fakeDataStore, TestCoroutineScope())
    }

    @Test
    fun `test CCLSettings - set last execution value and clear it again`() = runBlockingTest {
        cclSettings.getLastExecutionTime() shouldBe null

        val now = Instant.parse("2022-04-02T00:00:00.000Z")
        cclSettings.setExecutionTimeToNow(now)

        fakeDataStore[CCLSettings.LAST_EXECUTION_TIME_KEY] shouldBe now.seconds
        cclSettings.getLastExecutionTime() shouldBe now

        cclSettings.clear()
        cclSettings.getLastExecutionTime() shouldBe null
    }

    @Test
    fun `test CCLSettings - set admission scenario identifier`() = runBlockingTest {
        cclSettings.getAdmissionScenarioId() shouldBe ""

        cclSettings.setAdmissionScenarioId("Ad-Sc-ID")

        fakeDataStore[CCLSettings.ADMISSION_SCENARIO_ID_KEY] shouldBe "Ad-Sc-ID"
        cclSettings.getAdmissionScenarioId() shouldBe "Ad-Sc-ID"

        cclSettings.clear()
        cclSettings.getAdmissionScenarioId() shouldBe ""
    }
}
