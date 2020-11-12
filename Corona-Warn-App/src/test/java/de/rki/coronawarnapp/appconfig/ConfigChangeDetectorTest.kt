package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.task.TaskController
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ConfigChangeDetectorTest : BaseTest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var taskController: TaskController

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(LocalData)
        mockkObject(ConfigChangeDetector.RiskLevelRepositoryDeferrer)
        every { ConfigChangeDetector.RiskLevelRepositoryDeferrer.resetRiskLevel() } just Runs
        every { taskController.submit(any()) } just Runs
        every { LocalData.lastConfigId(any()) } just Runs
    }

    @Test
    fun `new idetifier results in new risk level calculation`() {
        every { LocalData.lastConfigId() } returns "A"
        ConfigChangeDetector(appConfigProvider, taskController).check("B")
        coVerifyOrder {
            ConfigChangeDetector.RiskLevelRepositoryDeferrer.resetRiskLevel()
            taskController.submit(any())
            LocalData.lastConfigId("B")
        }
    }

    @Test
    fun `same idetifier results in no op`() {
        every { LocalData.lastConfigId() } returns "B"
        ConfigChangeDetector(appConfigProvider, taskController).check("B")
        verify(exactly = 0) { ConfigChangeDetector.RiskLevelRepositoryDeferrer.resetRiskLevel() }
        verify(exactly = 0) { taskController.submit(any()) }
        verify(exactly = 0) { LocalData.lastConfigId(any()) }
    }
}
