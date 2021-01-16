package de.rki.coronawarnapp.util.device

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.test

class ForegroundStateTest : BaseTest() {

    @MockK lateinit var lifecycleOwner: LifecycleOwner
    lateinit var lifecycle: LifecycleRegistry

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        lifecycle = LifecycleRegistry(lifecycleOwner)
        every { lifecycleOwner.lifecycle } returns lifecycle
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    fun createInstance() = ForegroundState(
        processLifecycleOwner = lifecycleOwner
    )

    @Test
    fun `test emissions`() = runBlockingTest {
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
