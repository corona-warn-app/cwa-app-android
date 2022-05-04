package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.task.TaskController
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ConfigChangeDetectorTest : BaseTest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var taskController: TaskController
    @MockK lateinit var riskLevelSettings: RiskLevelSettings
    @MockK lateinit var riskLevelStorage: RiskLevelStorage

    private val currentConfigFake = MutableStateFlow(mockConfigId("initial"))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { taskController.submit(any()) } just Runs
        every { appConfigProvider.currentConfig } returns currentConfigFake
        coEvery { riskLevelStorage.clear() } just Runs
        coEvery { riskLevelStorage.clearResults() } just Runs
    }

    private fun mockConfigId(id: String): ConfigData {
        return mockk<ConfigData>().apply {
            every { identifier } returns id
        }
    }

    private fun createInstance() = ConfigChangeDetector(
        appConfigProvider = appConfigProvider,
        taskController = taskController,
        appScope = TestScope(),
        riskLevelSettings = riskLevelSettings,
        riskLevelStorage = riskLevelStorage
    )

    @Test
    fun `new identifier without previous one is ignored`() {

        every { riskLevelSettings.lastUsedConfigIdentifier } returns null

        createInstance().launch()

        coVerify(exactly = 0) {
            taskController.submit(any())
            riskLevelStorage.clearResults()
        }
    }

    @Test
    fun `new identifier results in new risk level calculation`() {
        every { riskLevelSettings.lastUsedConfigIdentifier } returns "I'm a new identifier"

        createInstance().launch()

        coVerifySequence {
            riskLevelStorage.clearResults()
            taskController.submit(any())
            taskController.submit(any())
        }

        coVerify(exactly = 0) {
            riskLevelStorage.clear()
        }
    }

    @Test
    fun `same identifier results in no op`() {
        every { riskLevelSettings.lastUsedConfigIdentifier } returns "initial"

        createInstance().launch()

        coVerify(exactly = 0) {
            taskController.submit(any())
            riskLevelStorage.clearResults()
        }
    }

    @Test
    fun `new emissions keep triggering the check`() {
        every { riskLevelSettings.lastUsedConfigIdentifier } returns "initial"

        createInstance().launch()
        currentConfigFake.value = mockConfigId("Straw")
        currentConfigFake.value = mockConfigId("berry")

        coVerifySequence {
            riskLevelStorage.clearResults()
            taskController.submit(any())
            taskController.submit(any())
            riskLevelStorage.clearResults()
            taskController.submit(any())
            taskController.submit(any())
        }

        coVerify(exactly = 0) {
            riskLevelStorage.clear()
        }
    }
}
