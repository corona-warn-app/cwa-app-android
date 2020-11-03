package de.rki.coronawarnapp.main

import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.ui.main.MainActivityViewModel
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class MainActivityViewModelTest : BaseTest() {

    @MockK lateinit var environmentSetup: EnvironmentSetup

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(CWADebug)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): MainActivityViewModel = MainActivityViewModel(
        dispatcherProvider = TestDispatcherProvider,
        environmentSetup = environmentSetup
    )

    @Test
    fun `environment toast is visible test environments`() {
        every { CWADebug.isDeviceForTestersBuild } returns true
        every { environmentSetup.currentEnvironment } returns EnvironmentSetup.Type.DEV

        val vm = createInstance()
        vm.showEnvironmentHint.value shouldBe EnvironmentSetup.Type.DEV.rawKey
    }

    @Test
    fun `environment toast is only visible in deviceForTesters flavor`() {
        every { CWADebug.isDeviceForTestersBuild } returns false
        every { environmentSetup.currentEnvironment } returns EnvironmentSetup.Type.DEV

        val vm = createInstance()
        vm.showEnvironmentHint.value shouldBe null
    }

    @Test
    fun `environment toast is not visible in production`() {
        every { CWADebug.isDeviceForTestersBuild } returns true
        every { environmentSetup.currentEnvironment } returns EnvironmentSetup.Type.PRODUCTION

        val vm = createInstance()
        vm.showEnvironmentHint.value shouldBe null
    }
}
