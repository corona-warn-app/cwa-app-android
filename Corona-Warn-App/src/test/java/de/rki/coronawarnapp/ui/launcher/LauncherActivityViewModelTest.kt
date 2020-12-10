package de.rki.coronawarnapp.ui.launcher

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
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class LauncherActivityViewModelTest {

    @MockK lateinit var updateChecker: UpdateChecker

    @BeforeEach
    fun setupFreshViewModel() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)
        every { LocalData.isOnboarded() } returns false

        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(isUpdateNeeded = false)
    }

    private fun createViewModel() = LauncherActivityViewModel(
        updateChecker = updateChecker,
        dispatcherProvider = TestDispatcherProvider
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

        val vm = createViewModel()

        vm.events.value shouldBe LauncherEvent.GoToMainActivity
    }
}
