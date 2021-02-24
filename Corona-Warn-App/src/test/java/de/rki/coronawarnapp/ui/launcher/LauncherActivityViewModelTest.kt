package de.rki.coronawarnapp.ui.launcher

import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.update.UpdateChecker
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
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
import testhelpers.preferences.mockFlowPreference

@ExtendWith(InstantExecutorExtension::class)
class LauncherActivityViewModelTest : BaseTest() {

    @MockK lateinit var updateChecker: UpdateChecker
    @MockK lateinit var cwaSettings: CWASettings

    @BeforeEach
    fun setupFreshViewModel() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)
        every { LocalData.isOnboarded() } returns false

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.VERSION_CODE } returns 10L

        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(isUpdateNeeded = false)
    }

    private fun createViewModel() = LauncherActivityViewModel(
        updateChecker = updateChecker,
        dispatcherProvider = TestDispatcherProvider(),
        cwaSettings = cwaSettings
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
        every { LocalData.isOnboarded() } returns true
        every { LocalData.isInteroperabilityShownAtLeastOnce } returns true
        every { cwaSettings.lastChangelogVersion } returns mockFlowPreference(10L)

        val vm = createViewModel()

        vm.events.value shouldBe LauncherEvent.GoToMainActivity
    }
}
