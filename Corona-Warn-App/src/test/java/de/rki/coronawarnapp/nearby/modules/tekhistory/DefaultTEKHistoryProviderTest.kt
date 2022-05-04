package de.rki.coronawarnapp.nearby.modules.tekhistory

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient.EXTRA_TEMPORARY_EXPOSURE_KEY_LIST
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.nearby.modules.version.ENFVersion
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.gms.MockGMSTask
import timber.log.Timber
import java.io.PrintWriter

class DefaultTEKHistoryProviderTest : BaseTest() {

    @MockK lateinit var client: ExposureNotificationClient
    @MockK lateinit var enfVersion: ENFVersion
    @MockK lateinit var context: Context

    private var currentReceiver: BroadcastReceiver? = null

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        /**
         * Setup defaults that equal the expected start state on a device
         * No authorization yet, no keys yet.
         * New devices are expected to have ENF Version 1.8+
         */
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true

        every { client.temporaryExposureKeyHistory } answers {
            MockGMSTask.forError(
                spyk(ApiException(Status(ExposureNotificationStatusCodes.RESOLUTION_REQUIRED))).apply {
                    every { status } returns mockk<Status>().apply {
                        every { hasResolution() } returns true
                    }
                }
            )
        }

        every { context.registerReceiver(any(), any()) } answers {
            currentReceiver = arg<BroadcastReceiver>(0)
            Timber.d("Received new receiver, calling onReceive with mock: %s", currentReceiver)
            val intent = mockk<Intent>().apply {
                every {
                    getParcelableArrayListExtra<TemporaryExposureKey>(EXTRA_TEMPORARY_EXPOSURE_KEY_LIST)
                } returns null
            }
            currentReceiver!!.onReceive(context, intent)
            mockk()
        }
        every { context.unregisterReceiver(any()) } answers {
            val receiver = arg<BroadcastReceiver>(0)
            if (receiver != currentReceiver) {
                throw IllegalArgumentException("That receiver is not registered.")
            } else {
                currentReceiver = null
            }
        }

        every { client.requestPreAuthorizedTemporaryExposureKeyHistory() } answers {
            MockGMSTask.forError(
                spyk(ApiException(Status(ExposureNotificationStatusCodes.RESOLUTION_REQUIRED))).apply {
                    every { status } returns mockk<Status>().apply {
                        every { hasResolution() } returns true
                    }
                }
            )
        }
        every { client.requestPreAuthorizedTemporaryExposureKeyRelease() } answers { MockGMSTask.forValue(null) }
    }

    private fun createInstance() = DefaultTEKHistoryProvider(
        client = client,
        enfVersion = enfVersion,
        context = context
    )

    @Test
    fun `ENFV1_7 init is side effect free and lazy`() = runTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns false
        createInstance()

        advanceUntilIdle()

        verify { client wasNot Called }
    }

    @Test
    fun `ENFV1_8 init is side effect free and lazy`() = runTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
        createInstance()

        advanceUntilIdle()

        verify { client wasNot Called }
    }

    @Test
    fun `ENFV1_7 errors are forwarded`() = runTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns false
        val error = ApiException(Status.RESULT_DEAD_CLIENT)
        every { client.temporaryExposureKeyHistory } answers { MockGMSTask.forError(error) }

        val instance = createInstance()

        shouldThrowExactly<ApiException> { instance.getTEKHistory() } shouldBe error

        verify { client.temporaryExposureKeyHistory }
        verify(exactly = 0) {
            client.requestPreAuthorizedTemporaryExposureKeyHistory()
            client.requestPreAuthorizedTemporaryExposureKeyRelease()
        }
    }

    @Test
    fun `ENFV1_7 getTEKHistory only calls the normal API`() = runTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns false
        val mockTEK = mockk<TemporaryExposureKey>()
        every { client.temporaryExposureKeyHistory } answers { MockGMSTask.forValue(listOf(mockTEK)) }

        createInstance().getTEKHistory() shouldBe listOf(mockTEK)

        verify { client.temporaryExposureKeyHistory }
        verify(exactly = 0) {
            client.requestPreAuthorizedTemporaryExposureKeyHistory()
            client.requestPreAuthorizedTemporaryExposureKeyRelease()
        }
    }

    @Test
    fun `ENFV1_7 preAuthorizeExposureKeyHistory return false on older Api`() = runTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns false

        createInstance().preAuthorizeExposureKeyHistory() shouldBe false
    }

    @Test
    fun `ENFV1_8 preAuthorizeExposureKeyHistory return true on newer Api`() = runTest {
        every { client.requestPreAuthorizedTemporaryExposureKeyHistory() } answers { MockGMSTask.forValue(null) }

        createInstance().preAuthorizeExposureKeyHistory() shouldBe true
    }

    @Test
    fun `ENFV1_8 getTEKHistory request keys from new Api`() = runTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
        every { client.requestPreAuthorizedTemporaryExposureKeyHistory() } answers { MockGMSTask.forValue(null) }

        createInstance().getTEKHistory()

        verify(exactly = 1) { client.requestPreAuthorizedTemporaryExposureKeyRelease() }
    }

    @Test
    fun `ENFV1_8 getTEKHistory throws ApiException on new Api`() = runTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
        val error = mockk<ApiException>().apply {
            every { status.hasResolution() } returns true
            every { printStackTrace(any<PrintWriter>()) } just Runs
        }
        every { client.requestPreAuthorizedTemporaryExposureKeyRelease() } answers { MockGMSTask.forError(error) }

        shouldThrow<ApiException> {
            createInstance().getTEKHistory()
        }
    }

    @Test
    fun `ENFV1_8 getTEKHistory request keys from new Api fallback to old Api on error`() = runTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
        every { client.requestPreAuthorizedTemporaryExposureKeyRelease() } answers { MockGMSTask.forError(Exception()) }
        val mockTEK = mockk<TemporaryExposureKey>()
        every { client.temporaryExposureKeyHistory } answers { MockGMSTask.forValue(listOf(mockTEK)) }

        createInstance().getTEKHistory() shouldBe listOf(mockTEK)

        verify(exactly = 1) { client.temporaryExposureKeyHistory }
        verify { client.requestPreAuthorizedTemporaryExposureKeyHistory() wasNot Called }
    }

    @Test
    fun `ENFV1_8 getTEKHistory request keys from new Api does not check for preAuth`() = runTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
        every { client.requestPreAuthorizedTemporaryExposureKeyRelease() } answers { MockGMSTask.forValue(null) }

        createInstance().getTEKHistory()

        verify(exactly = 1) { client.requestPreAuthorizedTemporaryExposureKeyRelease() }
        verify { client.requestPreAuthorizedTemporaryExposureKeyHistory() wasNot Called }
    }

    @Test
    fun `ENFV1_8 pre authorized key release timeout after 5 seconds`() {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
        every { client.requestPreAuthorizedTemporaryExposureKeyRelease() } returns MockGMSTask.timeout()
        verify(exactly = 0) { context.unregisterReceiver(any()) }

        runTest {
            val deferred = async { createInstance().getPreAuthorizedExposureKeys() }
            advanceTimeBy(6_000)
            deferred.getCompletionExceptionOrNull() shouldBe instanceOf(TimeoutCancellationException::class)
        }

        currentReceiver shouldBe null // We unregistered
        verify { context.unregisterReceiver(any()) }
    }

    @Test
    fun `ENFV1_8 pre authorized key release broadcast receiver timeout after 5 seconds`() {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true

        // We don't call onReceive
        every { context.registerReceiver(any(), any()) } answers {
            currentReceiver = arg(0)
            mockk()
        }

        verify(exactly = 0) { context.unregisterReceiver(any()) }

        runTest {
            val deferred = async { createInstance().getPreAuthorizedExposureKeys() }
            advanceTimeBy(6_000)
            deferred.getCompletionExceptionOrNull() shouldBe instanceOf(TimeoutCancellationException::class)
        }

        currentReceiver shouldBe null // We unregistered
        verify { context.unregisterReceiver(any()) }
    }

    @Test
    fun `ENFV1_7 getTEKHistoryOrRequestPermission - authorization required`() {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns false

        val onTEKHistoryAvailable = mockk<(List<TemporaryExposureKey>) -> Unit>(relaxed = true)
        val onPermissionRequired = mockk<(Status) -> Unit>(relaxed = true)

        runTest {
            createInstance().getTEKHistoryOrRequestPermission(
                onTEKHistoryAvailable = onTEKHistoryAvailable,
                onPermissionRequired = onPermissionRequired
            )
        }

        verify {
            onPermissionRequired.invoke(any())
            onTEKHistoryAvailable wasNot Called
        }
    }

    @Test
    fun `ENFV1_8 getTEKHistoryOrRequestPermission - if not preauthorized and no resolution then use the old API`() {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
        every { client.requestPreAuthorizedTemporaryExposureKeyRelease() } returns MockGMSTask.forError(Exception())

        val onTEKHistoryAvailable = mockk<(List<TemporaryExposureKey>) -> Unit>(relaxed = true)
        val onPermissionRequired = mockk<(Status) -> Unit>(relaxed = true)

        runTest {
            createInstance().getTEKHistoryOrRequestPermission(
                onTEKHistoryAvailable = onTEKHistoryAvailable,
                onPermissionRequired = onPermissionRequired
            )
        }

        verify {
            onPermissionRequired.invoke(any())
            onTEKHistoryAvailable wasNot Called
            client.temporaryExposureKeyHistory
        }
    }

    @Test
    fun `ENFV1_7 getTEKHistoryOrRequestPermission - API error on old api is rethrown`() {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns false
        every { client.temporaryExposureKeyHistory } returns MockGMSTask.forError(RuntimeException())

        runTest {
            shouldThrow<RuntimeException> {
                createInstance().getTEKHistoryOrRequestPermission(mockk(), mockk())
            }
        }
    }

    @Test
    fun `ENFV1_8 getTEKHistoryOrRequestPermission - error despite authorization causes us to try the old API`() {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true

        val mockTEK = mockk<TemporaryExposureKey>()
        every { client.temporaryExposureKeyHistory } returns MockGMSTask.forValue(listOf(mockTEK))
        every { client.requestPreAuthorizedTemporaryExposureKeyHistory() } returns MockGMSTask.forValue(null)
        every { client.requestPreAuthorizedTemporaryExposureKeyRelease() } returns
            MockGMSTask.forError(RuntimeException())

        val onTEKHistoryAvailable = mockk<(List<TemporaryExposureKey>) -> Unit>(relaxed = true)
        val onPermissionRequired = mockk<(Status) -> Unit>(relaxed = true)

        runTest {
            createInstance().getTEKHistoryOrRequestPermission(
                onTEKHistoryAvailable = onTEKHistoryAvailable,
                onPermissionRequired = onPermissionRequired
            )
        }

        verify {
            onTEKHistoryAvailable.invoke(listOf(mockTEK))
            onPermissionRequired wasNot Called
        }
        verifyOrder {
            client.requestPreAuthorizedTemporaryExposureKeyRelease()
            client.temporaryExposureKeyHistory
        }
    }

    @Test
    fun `ENFV1_8 getTEKHistoryOrRequestPermission - an error on all APIs is rethrown`() {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
        every { client.temporaryExposureKeyHistory } returns MockGMSTask.forError(IllegalStateException())
        every { client.requestPreAuthorizedTemporaryExposureKeyHistory() } returns MockGMSTask.forValue(null)
        every { client.requestPreAuthorizedTemporaryExposureKeyRelease() } returns
            MockGMSTask.forError(RuntimeException())

        runTest {
            shouldThrow<IllegalStateException> {
                createInstance().getTEKHistoryOrRequestPermission(mockk(), mockk())
            }
        }
    }
}
