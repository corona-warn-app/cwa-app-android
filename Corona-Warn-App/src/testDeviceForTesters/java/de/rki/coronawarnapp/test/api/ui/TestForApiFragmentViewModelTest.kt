package de.rki.coronawarnapp.test.api.ui

import android.content.Context
import androidx.lifecycle.Observer
import de.rki.coronawarnapp.environment.EnvironmentSetup
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.flakyTest
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class TestForApiFragmentViewModelTest : BaseTest() {

    @MockK private lateinit var environmentSetup: EnvironmentSetup
    @MockK private lateinit var context: Context

    private var currentEnvironment = EnvironmentSetup.EnvType.DEV

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        currentEnvironment = EnvironmentSetup.EnvType.DEV

        every { environmentSetup.defaultEnvironment } returns EnvironmentSetup.EnvType.DEV
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

    private fun createViewModel(): TestForApiFragmentViewModel = TestForApiFragmentViewModel(
        envSetup = environmentSetup,
        context = context
    )

    @Test
    fun `toggeling the env works`() = flakyTest {
        currentEnvironment = EnvironmentSetup.EnvType.DEV
        val vm = createViewModel()

        val states = mutableListOf<EnvironmentState>()
        val observerState = mockk<Observer<EnvironmentState>>()
        every { observerState.onChanged(capture(states)) } just Runs
        vm.environmentState.observeForever(observerState)

        val events = mutableListOf<EnvironmentSetup.EnvType>()
        val observerEvent = mockk<Observer<EnvironmentSetup.EnvType>>()
        every { observerEvent.onChanged(capture(events)) } just Runs
        vm.environmentChangeEvent.observeForever(observerEvent)

        vm.selectEnvironmentTytpe(EnvironmentSetup.EnvType.DEV.rawKey)
        vm.selectEnvironmentTytpe(EnvironmentSetup.EnvType.WRU_XA.rawKey)

        verify(exactly = 3, timeout = 3000) { observerState.onChanged(any()) }
        verify(exactly = 2, timeout = 3000) { observerEvent.onChanged(any()) }

        states[0].apply {
            current shouldBe EnvironmentSetup.EnvType.DEV
        }

        states[1].apply {
            current shouldBe EnvironmentSetup.EnvType.DEV
        }
        events[0] shouldBe EnvironmentSetup.EnvType.DEV


        states[2].apply {
            current shouldBe EnvironmentSetup.EnvType.WRU_XA
        }
        events[1] shouldBe EnvironmentSetup.EnvType.WRU_XA
    }
}
