package de.rki.coronawarnapp.test.debugoptions.ui

import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.eol.AppEol
import de.rki.coronawarnapp.eol.EolSetting
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class DebugOptionsFragmentViewModelTest : testhelpers.BaseTest() {

    @MockK private lateinit var environmentSetup: EnvironmentSetup
    @MockK private lateinit var environmentSunset: EnvironmentSunset
    @MockK private lateinit var eolSetting: EolSetting
    @MockK private lateinit var eol: AppEol

    private var currentEnvironment = EnvironmentSetup.Type.DEV

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        currentEnvironment = EnvironmentSetup.Type.DEV

        every { environmentSetup.defaultEnvironment } returns EnvironmentSetup.Type.DEV
        every { environmentSetup.submissionCdnUrl } returns "submissionUrl"
        every { environmentSetup.downloadCdnUrl } returns "downloadUrl"
        every { environmentSetup.verificationCdnUrl } returns "verificationUrl"
        every { environmentSetup.dataDonationCdnUrl } returns "dataDonationUrl"
        every { environmentSetup.logUploadServerUrl } returns "logUploadServerUrl"
        every { environmentSetup.crowdNotifierPublicKey } returns "crowdNotifierPublicKey"
        every { environmentSetup.appConfigPublicKey } returns "appConfigPublicKey"
        every { environmentSetup.dccServerUrl } returns "dccServerUrl"
        every { environmentSetup.dccReissuanceServerUrl } returns "dccReissuanceServerUrl"

        every { environmentSetup.currentEnvironment = any() } answers { currentEnvironment = arg(0) }
        every { environmentSetup.currentEnvironment } answers { currentEnvironment }
        every { environmentSetup.launchEnvironment } returns null
        every { eolSetting.isLoggerAllowed } returns flowOf(false)
        every { eol.isEol } returns flowOf(false)
    }

    private fun createViewModel(): DebugOptionsFragmentViewModel = DebugOptionsFragmentViewModel(
        envSetup = environmentSetup,
        dispatcherProvider = TestDispatcherProvider(),
        environmentSunset = environmentSunset,
        appScope = TestScope(),
        eolSetting = eolSetting,
        eol = eol
    )

    @Test
    fun `toggling the env works`() {
        currentEnvironment = EnvironmentSetup.Type.DEV
        val vm = createViewModel()
        vm.environmentState.getOrAwaitValue().current shouldBe EnvironmentSetup.Type.DEV

        vm.selectEnvironmentType(EnvironmentSetup.Type.DEV.rawKey)
        vm.environmentState.getOrAwaitValue().current shouldBe EnvironmentSetup.Type.DEV

        vm.selectEnvironmentType(EnvironmentSetup.Type.WRU_XA.rawKey)
        vm.environmentState.getOrAwaitValue().current shouldBe EnvironmentSetup.Type.WRU_XA
    }
}
