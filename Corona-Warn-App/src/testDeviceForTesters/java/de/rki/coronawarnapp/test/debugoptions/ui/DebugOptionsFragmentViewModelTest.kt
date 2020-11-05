package de.rki.coronawarnapp.test.debugoptions.ui

import android.content.Context
import androidx.lifecycle.Observer
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.test.api.ui.EnvironmentState
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.flakyTest

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class DebugOptionsFragmentViewModelTest : BaseTest() {

    @MockK private lateinit var environmentSetup: EnvironmentSetup
    @MockK private lateinit var context: Context
    @MockK private lateinit var testSettings: TestSettings
    @MockK lateinit var taskController: TaskController

    private var currentEnvironment = EnvironmentSetup.Type.DEV

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        currentEnvironment = EnvironmentSetup.Type.DEV

        every { environmentSetup.defaultEnvironment } returns EnvironmentSetup.Type.DEV
        every { environmentSetup.submissionCdnUrl } returns "submissionUrl"
        every { environmentSetup.downloadCdnUrl } returns "downloadUrl"
        every { environmentSetup.verificationCdnUrl } returns "verificationUrl"

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

    private fun createViewModel(): DebugOptionsFragmentViewModel = DebugOptionsFragmentViewModel(
        context = context,
        taskController = taskController,
        envSetup = environmentSetup,
        testSettings = testSettings,
        dispatcherProvider = TestDispatcherProvider
    )

    @Test
    fun `toggeling the env works`() = flakyTest {
        currentEnvironment = EnvironmentSetup.Type.DEV
        val vm = createViewModel()

        val states = mutableListOf<EnvironmentState>()
        val observerState = mockk<Observer<EnvironmentState>>()
        every { observerState.onChanged(capture(states)) } just Runs
        vm.environmentState.observeForever(observerState)

        val events = mutableListOf<EnvironmentSetup.Type>()
        val observerEvent = mockk<Observer<EnvironmentSetup.Type>>()
        every { observerEvent.onChanged(capture(events)) } just Runs
        vm.environmentChangeEvent.observeForever(observerEvent)

        vm.selectEnvironmentTytpe(EnvironmentSetup.Type.DEV.rawKey)
        vm.selectEnvironmentTytpe(EnvironmentSetup.Type.WRU_XA.rawKey)

        verify(exactly = 3, timeout = 3000) { observerState.onChanged(any()) }
        verify(exactly = 2, timeout = 3000) { observerEvent.onChanged(any()) }

        states[0].apply {
            current shouldBe EnvironmentSetup.Type.DEV
        }

        states[1].apply {
            current shouldBe EnvironmentSetup.Type.DEV
        }
        events[0] shouldBe EnvironmentSetup.Type.DEV

        states[2].apply {
            current shouldBe EnvironmentSetup.Type.WRU_XA
        }
        events[1] shouldBe EnvironmentSetup.Type.WRU_XA
    }
}
