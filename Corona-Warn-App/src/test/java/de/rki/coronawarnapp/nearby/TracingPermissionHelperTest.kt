package de.rki.coronawarnapp.nearby

import de.rki.coronawarnapp.storage.LocalData
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
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
        coEvery { enfClient.setTracing(any(), any(), any(), any()) } just Runs

        mockkObject(LocalData)
        every { LocalData.initialTracingActivationTimestamp() } returns 123L
    }

    fun createInstance(scope: CoroutineScope, callback: TracingPermissionHelper.Callback) = TracingPermissionHelper(
        callback = callback,
        scope = scope,
        enfClient = enfClient
    )

    @Test
    fun `request is not forwarded if tracing is enabled`() = runBlockingTest {
        coEvery { enfClient.isTracingEnabled } returns flowOf(true)

        val callback = mockk<TracingPermissionHelper.Callback>(relaxUnitFun = true)
        val instance = createInstance(scope = this, callback = callback)

        instance.startTracing()

        advanceUntilIdle()

        coVerifySequence {
            callback.onUpdateTracingStatus(true)
        }
    }

    @Test
    fun `if consent is missing then we continue after it was given`() = runBlockingTest {
        every { LocalData.initialTracingActivationTimestamp() } returns null

        val callback = mockk<TracingPermissionHelper.Callback>(relaxUnitFun = true)
        val consentCallbackSlot = slot<(Boolean) -> Unit>()
        every { callback.onTracingConsentRequired(capture(consentCallbackSlot)) } just Runs
        val instance = createInstance(scope = this, callback = callback)

        instance.startTracing()

        consentCallbackSlot.captured(true)

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.setTracing(
                enable = true,
                onSuccess = any(),
                onError = any(),
                onPermissionRequired = any()
            )
        }
    }

    @Test
    fun `if consent was declined then we do nothing`() = runBlockingTest {
        every { LocalData.initialTracingActivationTimestamp() } returns null

        val callback = mockk<TracingPermissionHelper.Callback>(relaxUnitFun = true)
        val consentCallbackSlot = slot<(Boolean) -> Unit>()
        every { callback.onTracingConsentRequired(capture(consentCallbackSlot)) } just Runs
        val instance = createInstance(scope = this, callback = callback)

        instance.startTracing()

        consentCallbackSlot.captured(false)

        coVerifySequence {
            enfClient.isTracingEnabled
        }
    }

    @Test
    fun `request is forwarded if tracing is disabled`() = runBlockingTest {
        val callback = mockk<TracingPermissionHelper.Callback>(relaxUnitFun = true)
        val instance = createInstance(scope = this, callback = callback)

        instance.startTracing()

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.setTracing(
                enable = true,
                onSuccess = any(),
                onError = any(),
                onPermissionRequired = any()
            )
        }
    }
}
