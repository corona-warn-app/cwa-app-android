package de.rki.coronawarnapp.ui.launcher

import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.rootdetection.RootDetectionCheck
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.update.UpdateChecker
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import testhelpers.preferences.mockFlowPreference

@ExtendWith(InstantExecutorExtension::class)
class LauncherActivityViewModelTest : BaseTest() {

    @MockK lateinit var updateChecker: UpdateChecker
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var onboardingSettings: OnboardingSettings
    @MockK lateinit var rootDetectionCheck: RootDetectionCheck

    @BeforeEach
    fun setupFreshViewModel() {
        MockKAnnotations.init(this)

        every { onboardingSettings.isOnboarded } returns false

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.VERSION_CODE } returns 10L

        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(isUpdateNeeded = false)
        coEvery { rootDetectionCheck.isRooted() } returns false
    }

    private fun createViewModel() = LauncherActivityViewModel(
        updateChecker = updateChecker,
        dispatcherProvider = TestDispatcherProvider(),
        cwaSettings = cwaSettings,
        onboardingSettings = onboardingSettings,
        rootDetectionCheck = rootDetectionCheck
    )

    @Test
    fun `update is available`() = runBlockingTest {
        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(
            isUpdateNeeded = true,
            updateIntent = { mockk() }
        )

        val vm = createViewModel()

        vm.events.value shouldBe instanceOf(LauncherEvent.ShowUpdateDialog::class)
    }

    @Test
    fun `fresh install no update needed`() {
        val vm = createViewModel()

        vm.events.value shouldBe LauncherEvent.GoToOnboarding
    }

    @Test
    fun `onboarding finished`() {
        every { onboardingSettings.isOnboarded } returns true
        every { cwaSettings.wasInteroperabilityShownAtLeastOnce } returns true
        every { cwaSettings.lastChangelogVersion } returns mockFlowPreference(10L)

        val vm = createViewModel()

        vm.events.value shouldBe LauncherEvent.GoToMainActivity
    }

    @Test
    fun `rooted device triggers root dialog`() {
        coEvery { rootDetectionCheck.isRooted() } returns true
        createViewModel().run {
            events.getOrAwaitValue() shouldBe LauncherEvent.ShowRootedDialog
        }

        coVerify {
            rootDetectionCheck.isRooted()
        }
    }

    @Test
    fun `onRootedDialogDismiss triggers update check`() {
        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(isUpdateNeeded = true)
        createViewModel().run {
            onRootedDialogDismiss()
            events.getOrAwaitValue() shouldBe instanceOf(LauncherEvent.ShowUpdateDialog::class)
        }
    }
}
