package de.rki.coronawarnapp.test.debugoptions.ui

import de.rki.coronawarnapp.environment.EnvironmentSetup
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTestInstrumentation
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class DebugOptionsFragmentViewModelTest : BaseTestInstrumentation() {

    @MockK private lateinit var environmentSetup: EnvironmentSetup

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
        every { environmentSetup.vaccinationProofServerUrl } returns "vaccinationProofServerUrl"
        every { environmentSetup.vaccinationCdnUrl } returns "vaccinationCdnUrl"
        every { environmentSetup.crowdNotifierPublicKey } returns "crowdNotifierPublicKey"
        every { environmentSetup.appConfigPublicKey } returns "appConfigPublicKey"

        every { environmentSetup.currentEnvironment = any() } answers {
            currentEnvironment = arg(0)
        }
        every { environmentSetup.currentEnvironment } answers {
            currentEnvironment
        }
    }

    private fun createViewModel(): DebugOptionsFragmentViewModel = DebugOptionsFragmentViewModel(
        envSetup = environmentSetup,
        dispatcherProvider = TestDispatcherProvider()
    )

    @Test
    fun `toggeling the env works`() {
        currentEnvironment = EnvironmentSetup.Type.DEV
        val vm = createViewModel()
        vm.environmentState.getOrAwaitValue().current shouldBe EnvironmentSetup.Type.DEV

        vm.selectEnvironmentTytpe(EnvironmentSetup.Type.DEV.rawKey)
        vm.environmentState.getOrAwaitValue().current shouldBe EnvironmentSetup.Type.DEV

        vm.selectEnvironmentTytpe(EnvironmentSetup.Type.WRU_XA.rawKey)
        vm.environmentState.getOrAwaitValue().current shouldBe EnvironmentSetup.Type.WRU_XA
    }
}
