package de.rki.coronawarnapp.test.api.ui

import de.rki.coronawarnapp.environment.EnvironmentSetup
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class TestForApiFragmentViewModelTest : BaseTest() {

    @MockK private lateinit var environmentSetup: EnvironmentSetup

    var currentEnvironment = EnvironmentSetup.Type.DEV

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { environmentSetup.defaultEnvironment } returns EnvironmentSetup.Type.DEV
        every { environmentSetup.alternativeEnvironment } returns EnvironmentSetup.Type.WRU_XA

        every { environmentSetup.currentEnvironment = any() } answers {
            currentEnvironment = arg(0)
            Unit
        }
        every { environmentSetup.currentEnvironment } answers {
            currentEnvironment
        }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createViewModel(): TestForApiFragmentViewModel {
        return TestForApiFragmentViewModel(environmentSetup)
    }

    @Test
    fun `toggeling the env works`() {
        val vm = createViewModel()

        currentEnvironment = EnvironmentSetup.Type.DEV
        vm.isTestCountyCurrentEnvironment() shouldBe false
        currentEnvironment = EnvironmentSetup.Type.WRU_XA
        vm.isTestCountyCurrentEnvironment() shouldBe true

        vm.environmentChangeEvent.value shouldBe null
        vm.toggleEnvironment(true)
        vm.environmentChangeEvent.value shouldBe EnvironmentSetup.Type.WRU_XA
        verify { environmentSetup.currentEnvironment = EnvironmentSetup.Type.WRU_XA }
        vm.toggleEnvironment(false)
        verify { environmentSetup.currentEnvironment = EnvironmentSetup.Type.DEV }
    }
}
