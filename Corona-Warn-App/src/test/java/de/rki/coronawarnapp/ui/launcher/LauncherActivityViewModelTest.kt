package de.rki.coronawarnapp.ui.launcher

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.rootdetection.RootDetectionCheck
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.update.UpdateChecker
import de.rki.coronawarnapp.update.getUpdateInfo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.instanceOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
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
    @MockK lateinit var appUpdateManager: AppUpdateManager
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var onboardingSettings: OnboardingSettings
    @MockK lateinit var rootDetectionCheck: RootDetectionCheck

    @BeforeEach
    fun setupFreshViewModel() {
        MockKAnnotations.init(this)
        mockkStatic("de.rki.coronawarnapp.update.InAppUpdateKt")

        every { onboardingSettings.isOnboarded } returns false

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.VERSION_CODE } returns 10L

        coEvery { appUpdateManager.getUpdateInfo() } returns
            mockk<AppUpdateInfo>().apply {
                every { updateAvailability() } returns UpdateAvailability.UPDATE_NOT_AVAILABLE
            }

        every {
            appUpdateManager.startUpdateFlowForResult(
                any(),
                AppUpdateType.IMMEDIATE,
                any<Activity>(),
                any()
            )
        } returns true

        coEvery { rootDetectionCheck.isRooted() } returns false
        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(isUpdateNeeded = false)
    }

    private fun createViewModel() = LauncherActivityViewModel(
        updateChecker = updateChecker,
        dispatcherProvider = TestDispatcherProvider(),
        cwaSettings = cwaSettings,
        onboardingSettings = onboardingSettings,
        rootDetectionCheck = rootDetectionCheck,
        appUpdateManager = appUpdateManager
    )

    @Test
    fun `Force update - Works only if AppConfig + InAppUpdate are requiring and update`() = runBlockingTest {
        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(isUpdateNeeded = true)
        coEvery { appUpdateManager.getUpdateInfo() } returns
            mockk<AppUpdateInfo>().apply {
                every { updateAvailability() } returns UpdateAvailability.UPDATE_AVAILABLE
            }
        val vm = createViewModel()

        vm.events.value shouldBe instanceOf(LauncherEvent.ForceUpdate::class)
    }

    @Test
    fun `Force update - NOT triggered if InAppUpdate info is missing`() = runBlockingTest {
        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(isUpdateNeeded = true)
        coEvery { appUpdateManager.getUpdateInfo() } returns null
        val vm = createViewModel()

        vm.events.value shouldNotBe instanceOf(LauncherEvent.ForceUpdate::class)
    }

    @Test
    fun `Force update - NOT triggered if AppConfig is not enabled`() = runBlockingTest {
        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(isUpdateNeeded = false)
        coEvery { appUpdateManager.getUpdateInfo() } returns
            mockk<AppUpdateInfo>().apply {
                every { updateAvailability() } returns UpdateAvailability.UPDATE_AVAILABLE
            }
        val vm = createViewModel()

        vm.events.value shouldNotBe instanceOf(LauncherEvent.ForceUpdate::class)
    }

    @Test
    fun `Force update - NOT triggered if no InAppUpdate available`() = runBlockingTest {
        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(isUpdateNeeded = true)
        coEvery { appUpdateManager.getUpdateInfo() } returns
            mockk<AppUpdateInfo>().apply {
                every { updateAvailability() } returns UpdateAvailability.UPDATE_NOT_AVAILABLE
            }
        val vm = createViewModel()

        vm.events.value shouldNotBe instanceOf(LauncherEvent.ForceUpdate::class)
    }

    @Test
    fun `Force update Error - Asks user to try again`() {
        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(isUpdateNeeded = true)
        coEvery { appUpdateManager.getUpdateInfo() } returns mockk<AppUpdateInfo>().apply {
            every { updateAvailability() } returns UpdateAvailability.UPDATE_AVAILABLE
        }

        every {
            appUpdateManager.startUpdateFlowForResult(
                any(),
                AppUpdateType.IMMEDIATE,
                any<Activity>(),
                any()
            )
        } throws Exception("Crash!")

        val vm = createViewModel()
        vm.requestUpdate()
        (vm.events.value as LauncherEvent.ForceUpdate).apply {
            forceUpdate(mockk())
            vm.events.getOrAwaitValue() shouldBe LauncherEvent.ShowUpdateDialog
        }
    }

    @Test
    fun `onResume update is resumed`() {
        coEvery { appUpdateManager.getUpdateInfo() } returns mockk<AppUpdateInfo>().apply {
            every { updateAvailability() } returns UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        }
        val vm = createViewModel()
        vm.onResume()
        (vm.events.value as LauncherEvent.ForceUpdate).apply {
            forceUpdate(mockk())
            verify {
                appUpdateManager.startUpdateFlowForResult(
                    any(),
                    AppUpdateType.IMMEDIATE,
                    any<Activity>(),
                    any()
                )
            }
        }
    }

    @Test
    fun `onResult nothing happens if requestCode is different`() {
        val vm = createViewModel()
        vm.onResult(1000, Activity.RESULT_OK)
        vm.events.value shouldBe LauncherEvent.GoToOnboarding // From initialization
    }

    @Test
    fun `onResult nothing happens if resultCode is OK`() {
        val vm = createViewModel()
        vm.onResult(LauncherActivityViewModel.UPDATE_CODE, Activity.RESULT_OK)
        vm.events.value shouldBe LauncherEvent.GoToOnboarding // From initialization
    }

    @Test
    fun `onResult ask user to update if resultCode is CANCLED`() {
        val vm = createViewModel()
        vm.onResult(LauncherActivityViewModel.UPDATE_CODE, Activity.RESULT_CANCELED)
        vm.events.value shouldBe LauncherEvent.ShowUpdateDialog
    }

    @Test
    fun `onResult ask user to update if resultCode is RESULT_IN_APP_UPDATE_FAILED`() {
        val vm = createViewModel()
        vm.onResult(LauncherActivityViewModel.UPDATE_CODE, ActivityResult.RESULT_IN_APP_UPDATE_FAILED)
        vm.events.value shouldBe LauncherEvent.ShowUpdateDialog
    }

    @Test
    fun `requestUpdate event triggers update`() {
        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(isUpdateNeeded = true)
        coEvery { appUpdateManager.getUpdateInfo() } returns mockk<AppUpdateInfo>().apply {
            every { updateAvailability() } returns UpdateAvailability.UPDATE_AVAILABLE
        }
        val vm = createViewModel()
        vm.requestUpdate()

        (vm.events.value as LauncherEvent.ForceUpdate).apply {
            forceUpdate(mockk())
            verify {
                appUpdateManager.startUpdateFlowForResult(
                    any(),
                    AppUpdateType.IMMEDIATE,
                    any<Activity>(),
                    any()
                )
            }
        }
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
        coEvery { appUpdateManager.getUpdateInfo() } returns
            mockk<AppUpdateInfo>().apply {
                every { updateAvailability() } returns UpdateAvailability.UPDATE_AVAILABLE
            }

        createViewModel().run {
            onRootedDialogDismiss()
            events.getOrAwaitValue() shouldBe instanceOf(LauncherEvent.ForceUpdate::class)
        }
    }
}
