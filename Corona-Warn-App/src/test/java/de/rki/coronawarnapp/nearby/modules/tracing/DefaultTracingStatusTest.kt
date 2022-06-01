package de.rki.coronawarnapp.nearby.modules.tracing

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.storage.TracingSettings
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.coroutines.test
import testhelpers.gms.MockGMSTask

class DefaultTracingStatusTest : BaseTest() {

    @MockK lateinit var client: ExposureNotificationClient
    @MockK lateinit var tracingSettings: TracingSettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { client.isEnabled } answers { MockGMSTask.forValue(true) }
        every { tracingSettings.isConsentGiven = any() } just Runs
    }

    private fun createInstance(scope: CoroutineScope): DefaultTracingStatus = DefaultTracingStatus(
        client = client,
        scope = scope,
        tracingSettings = tracingSettings
    )

    @Test
    fun `init is sideeffect free and lazy`() = runTest2 {
        createInstance(scope = this)

        advanceUntilIdle()

        verify { client wasNot Called }
    }

    @Test
    fun `state emission works`() = runTest2 {
        val instance = createInstance(scope = this)
        instance.isTracingEnabled.first() shouldBe true
    }

    @Test
    fun `state is updated and polling stops on cancel`() = runTest2 {
        every { client.isEnabled } returnsMany listOf(
            true,
            false,
            true,
            false,
            true,
            false,
            true
        ).map { MockGMSTask.forValue(it) }

        val instance = createInstance(scope = this)

        instance.isTracingEnabled.take(6).toList() shouldBe listOf(
            true,
            false,
            true,
            false,
            true,
            false
        )
        verify(exactly = 6) { client.isEnabled }
    }

    @Test
    fun `subscriptions are shared but not cached`() = runTest2 {
        val instance = createInstance(scope = this)

        val collector1 = instance.isTracingEnabled.test(tag = "1", startOnScope = this)
        val collector2 = instance.isTracingEnabled.test(tag = "2", startOnScope = this)

        delay(500)

        collector1.latestValue shouldBe true
        collector2.latestValue shouldBe true

        collector1.cancel()
        collector2.cancel()

        advanceUntilIdle()

        verify(exactly = 1) { client.isEnabled }

        every { client.isEnabled } answers { MockGMSTask.forValue(false) }
        instance.isTracingEnabled.first() shouldBe false
    }

    @Test
    fun `api errors during state polling are mapped to false`() = runTest2 {
        every { client.isEnabled } answers { MockGMSTask.forError(ApiException(Status.RESULT_INTERNAL_ERROR)) }

        val instance = createInstance(scope = this)

        instance.isTracingEnabled.first() shouldBe false
    }

    @Test
    fun `api errors during state setting are rethrown`() = runTest2 {
        val ourError = ApiException(Status.RESULT_INTERNAL_ERROR)
        every { client.start() } answers { MockGMSTask.forError(ourError) }
        every { client.isEnabled } answers { MockGMSTask.forValue(false) }

        val instance = createInstance(scope = this)

        var thrownError: Throwable? = null
        instance.setTracing(
            enable = true,
            onSuccess = {},
            onError = { thrownError = it },
            onPermissionRequired = {}
        )

        advanceUntilIdle()

        thrownError shouldBe ourError
    }

    @Test
    fun `extension for disabling tracing if enabled`() {
        val enabledFlow = MutableStateFlow(false)
        val tracingStatus = mockk<TracingStatus>().apply {
            every { isTracingEnabled } returns enabledFlow
            every { setTracing(any(), any(), any(), any()) } answers {
                val onSuccess = arg<(Boolean) -> Unit>(1)
                onSuccess(false)
            }
        }

        runTest {
            tracingStatus.disableTracingIfEnabled()
            verify(exactly = 0) { tracingStatus.setTracing(any(), any(), any(), any()) }
        }

        enabledFlow.value = true

        runTest {
            tracingStatus.disableTracingIfEnabled() shouldBe true
            verify(exactly = 1) { tracingStatus.setTracing(any(), any(), any(), any()) }
        }
    }
}
