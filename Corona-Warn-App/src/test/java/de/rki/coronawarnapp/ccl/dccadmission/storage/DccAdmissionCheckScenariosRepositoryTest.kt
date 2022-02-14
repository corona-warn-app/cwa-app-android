package de.rki.coronawarnapp.ccl.dccadmission.storage

import de.rki.coronawarnapp.ccl.configuration.update.CCLSettings
import de.rki.coronawarnapp.ccl.dccadmission.model.admissionCheckScenarios
import de.rki.coronawarnapp.ccl.dccadmission.model.scenariosJson
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import testhelpers.extensions.toComparableJson

class DccAdmissionCheckScenariosRepositoryTest : BaseTest() {

    private val mapper = SerializationModule().jacksonObjectMapper()

    @MockK lateinit var cclSettings: CCLSettings

    lateinit var instance: DccAdmissionCheckScenariosRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { cclSettings.setAdmissionCheckScenarios(any()) } just Runs
        every { cclSettings.admissionCheckScenarios } returns flowOf(null)
    }

    @Test
    fun `parse scenarios`() = runBlockingTest2 {
        every { cclSettings.admissionCheckScenarios } returns flowOf(scenariosJson)
        instance = createInstance()
        instance.admissionCheckScenarios.first() shouldBe admissionCheckScenarios
    }

    @Test
    fun `parse null`() = runBlockingTest2 {
        every { cclSettings.admissionCheckScenarios } returns flowOf(null)
        instance = createInstance()
        instance.admissionCheckScenarios.first() shouldBe null
    }

    @Test
    fun `parse empty string`() = runBlockingTest2 {
        every { cclSettings.admissionCheckScenarios } returns flowOf("")
        instance = createInstance()
        instance.admissionCheckScenarios.first() shouldBe null
    }

    @Test
    fun `parse corrupt string`() = runBlockingTest2 {
        every { cclSettings.admissionCheckScenarios } returns flowOf("something else")
        instance = createInstance()
        instance.admissionCheckScenarios.first() shouldBe null
    }

    @Test
    fun `save works`() = runBlockingTest2 {
        instance = createInstance()
        instance.save(admissionCheckScenarios)
        coVerify {
            cclSettings.setAdmissionCheckScenarios(scenariosJson.toComparableJson())
        }
    }

    @Test
    fun `clear works`() = runBlockingTest2 {
        instance = createInstance()
        instance.clear()
        coVerify {
            cclSettings.setAdmissionCheckScenarios("")
        }
    }

    private fun createInstance() = DccAdmissionCheckScenariosRepository(cclSettings, mapper)
}
