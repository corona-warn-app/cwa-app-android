package de.rki.coronawarnapp.nearby

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TracingPermissionHelperTest : BaseTest() {
    @MockK lateinit var enfClient: ENFClient

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { enfClient.isTracingEnabled } returns flowOf(false)
    }

    fun createInstance(scope: CoroutineScope, callback: TracingPermissionHelper.Callback) = TracingPermissionHelper(
        callback = callback,
        scope = scope,
        enfClient = enfClient
    )

    @Test
    fun `request is forwarded if tracing is disabled`() = runBlockingTest {
//        TODO()
    }

    @Test
    fun `request is not forwarded if tracing is enabled`() = runBlockingTest {
        coEvery { enfClient.isTracingEnabled } returns flowOf(true)

        val callback = mockk<TracingPermissionHelper.Callback>()
        every { callback.onUpdateTracingStatus(any()) } just Runs

        val instance = createInstance(scope = this, callback = callback)

        instance.startTracing()

        advanceUntilIdle()

        verify { callback.onUpdateTracingStatus(true) }
    }
}
