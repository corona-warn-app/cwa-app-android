package de.rki.coronawarnapp.util.device

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.coroutines.test
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class ForegroundStateTest : BaseTest() {

    @MockK lateinit var lifecycleOwner: LifecycleOwner
    lateinit var lifecycle: LifecycleRegistry

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        lifecycle = LifecycleRegistry(lifecycleOwner)
        every { lifecycleOwner.lifecycle } returns lifecycle
    }

    fun createInstance() = ForegroundState(
        processLifecycleOwner = lifecycleOwner
    )

    @Test
    fun `test emissions`() = runTest {
        val instance = createInstance()

        val testCollector = instance.isInForeground.test(startOnScope = this)

        testCollector.latestValue shouldBe false

        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        testCollector.latestValue shouldBe true

        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        testCollector.latestValue shouldBe false

        testCollector.cancel()
        advanceUntilIdle()
    }
}
