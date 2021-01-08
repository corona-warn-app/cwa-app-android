package de.rki.coronawarnapp.nearby

import android.app.Activity
import com.google.android.gms.common.api.Status
import de.rki.coronawarnapp.storage.LocalData
import io.kotest.matchers.shouldBe
import io.mockk.Called
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
    fun `if tracing is not yet enabled we forward to the enf client`() = runBlockingTest {
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

    @Test
    fun `permission request is forwarded from enf client`() = runBlockingTest {
        val callback = mockk<TracingPermissionHelper.Callback>(relaxUnitFun = true)

        val onPermissionRequiredCallback = slot<(Status) -> Unit>()
        coEvery {
            enfClient.setTracing(
                enable = true,
                onSuccess = any(),
                onError = any(),
                onPermissionRequired = capture(onPermissionRequiredCallback)
            )
        } just Runs
        val instance = createInstance(scope = this, callback = callback)

        instance.startTracing()
        onPermissionRequiredCallback.captured.invoke(mockk())

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.setTracing(
                enable = true,
                onSuccess = any(),
                onError = any(),
                onPermissionRequired = onPermissionRequiredCallback.captured
            )
            callback.onPermissionRequired(any())
        }
    }

    @Test
    fun `errors from the enf client are forwarded`() = runBlockingTest {
        val callback = mockk<TracingPermissionHelper.Callback>(relaxUnitFun = true)
        val onErrorCallback = slot<(Throwable) -> Unit>()
        coEvery {
            enfClient.setTracing(
                enable = true,
                onSuccess = any(),
                onError = capture(onErrorCallback),
                onPermissionRequired = any()
            )
        } just Runs
        val instance = createInstance(scope = this, callback = callback)

        instance.startTracing()
        val error = IllegalStateException()
        onErrorCallback.captured.invoke(error)

        coVerifySequence {
            enfClient.isTracingEnabled
            enfClient.setTracing(
                enable = true,
                onSuccess = any(),
                onError = onErrorCallback.captured,
                onPermissionRequired = any()
            )
            callback.onError(error)
        }
    }

    @Test
    fun `unknown activity results are not consumed`() = runBlockingTest {
        val callback = mockk<TracingPermissionHelper.Callback>(relaxUnitFun = true)
        val instance = createInstance(scope = this, callback = callback)

        instance.handleActivityResult(9999, Activity.RESULT_OK, mockk()) shouldBe false

        verify { callback wasNot Called }
    }

    @Test
    fun `positive activity results lead to new setTracing call`() = runBlockingTest {
        val callback = mockk<TracingPermissionHelper.Callback>(relaxUnitFun = true)
        val instance = createInstance(scope = this, callback = callback)

        instance.handleActivityResult(
            TracingPermissionHelper.TRACING_PERMISSION_REQUESTCODE,
            Activity.RESULT_OK,
            mockk()
        ) shouldBe true

        coVerifySequence {
            enfClient.setTracing(
                enable = true,
                onSuccess = any(),
                onError = any(),
                onPermissionRequired = any()
            )
            callback wasNot Called
        }
    }

    @Test
    fun `negative activity results lead permission to direct callback`() = runBlockingTest {
        val callback = mockk<TracingPermissionHelper.Callback>(relaxUnitFun = true)
        val instance = createInstance(scope = this, callback = callback)

        instance.handleActivityResult(
            TracingPermissionHelper.TRACING_PERMISSION_REQUESTCODE,
            Activity.RESULT_CANCELED,
            mockk()
        ) shouldBe true

        coVerifySequence {
            callback.onUpdateTracingStatus(false)
        }
    }
}
