package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.risk.RiskLevelData
import de.rki.coronawarnapp.task.TaskController
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ConfigChangeDetectorTest : BaseTest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var taskController: TaskController
    @MockK lateinit var riskLevelData: RiskLevelData

    private val currentConfigFake = MutableStateFlow(mockConfigId("initial"))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(ConfigChangeDetector.RiskLevelRepositoryDeferrer)
        every { ConfigChangeDetector.RiskLevelRepositoryDeferrer.resetRiskLevel() } just Runs

        every { taskController.submit(any()) } just Runs
        every { appConfigProvider.currentConfig } returns currentConfigFake
    }

    private fun mockConfigId(id: String): ConfigData {
        return mockk<ConfigData>().apply {
            every { identifier } returns id
        }
    }

    private fun createInstance() = ConfigChangeDetector(
        appConfigProvider = appConfigProvider,
        taskController = taskController,
        appScope = TestCoroutineScope(),
        riskLevelData = riskLevelData
    )

    @Test
    fun `new identifier without previous one is ignored`() {

        every { riskLevelData.lastUsedConfigIdentifier } returns null

        createInstance().launch()

        verify(exactly = 0) {
            taskController.submit(any())
            ConfigChangeDetector.RiskLevelRepositoryDeferrer.resetRiskLevel()
        }
    }

    @Test
    fun `new identifier results in new risk level calculation`() {
        every { riskLevelData.lastUsedConfigIdentifier } returns "I'm a new identifier"

        createInstance().launch()

        verifySequence {
            ConfigChangeDetector.RiskLevelRepositoryDeferrer.resetRiskLevel()
            taskController.submit(any())
        }
    }

    @Test
    fun `same idetifier results in no op`() {
        every { riskLevelData.lastUsedConfigIdentifier } returns "initial"

        createInstance().launch()

        verify(exactly = 0) {
            taskController.submit(any())
            ConfigChangeDetector.RiskLevelRepositoryDeferrer.resetRiskLevel()
        }
    }

    @Test
    fun `new emissions keep triggering the check`() {
        every { riskLevelData.lastUsedConfigIdentifier } returns "initial"

        createInstance().launch()
        currentConfigFake.value = mockConfigId("Straw")
        currentConfigFake.value = mockConfigId("berry")

        verifySequence {
            ConfigChangeDetector.RiskLevelRepositoryDeferrer.resetRiskLevel()
            taskController.submit(any())
            ConfigChangeDetector.RiskLevelRepositoryDeferrer.resetRiskLevel()
            taskController.submit(any())
        }
    }
}
