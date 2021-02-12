package de.rki.coronawarnapp.nearby.modules.tekhistory

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.nearby.modules.version.ENFVersion
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.gms.MockGMSTask
import java.io.PrintWriter
import java.lang.Exception

class DefaultTEKHistoryProviderTest : BaseTest() {

    @MockK lateinit var client: ExposureNotificationClient
    @MockK lateinit var enfVersion: ENFVersion
    @MockK lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { client.temporaryExposureKeyHistory } answers { MockGMSTask.forValue(listOf(mockk())) }
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns false
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = DefaultTEKHistoryProvider(
        client = client,
        enfVersion = enfVersion,
        context = context
    )

    @Test
    fun `init is side effect free and lazy`() = runBlockingTest {
        createInstance()

        advanceUntilIdle()

        verify { client wasNot Called }
    }

    @Test
    fun `history is forwarded`() = runBlockingTest {
        val instance = createInstance()
        instance.getTEKHistory().size shouldBe 1
    }

    @Test
    fun `errors are forwarded`() = runBlockingTest {
        val error = ApiException(Status.RESULT_DEAD_CLIENT)
        every { client.temporaryExposureKeyHistory } answers { MockGMSTask.forError(error) }
        val instance = createInstance()

        shouldThrowExactly<ApiException> { instance.getTEKHistory() } shouldBe error
    }

    @Test
    fun `preAuthorizeExposureKeyHistory return false on older Api`() = runBlockingTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns false

        createInstance().preAuthorizeExposureKeyHistory() shouldBe false
    }

    @Test
    fun `preAuthorizeExposureKeyHistory return true on newer Api`() = runBlockingTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
        every { client.requestPreAuthorizedTemporaryExposureKeyHistory() } answers {
            MockGMSTask.forValueOnComplete(null)
        }
        createInstance().preAuthorizeExposureKeyHistory() shouldBe true
    }

    @Test
    fun `getTEKHistory request keys from old Api`() = runBlockingTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns false
        createInstance().getTEKHistory()
        verify { client.temporaryExposureKeyHistory }
    }

    @Test
    fun `getTEKHistory request keys from new Api`() = runBlockingTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
        every { client.requestPreAuthorizedTemporaryExposureKeyHistory() } answers {
            MockGMSTask.forValueOnComplete(null)
        }
        createInstance().getTEKHistory()
        verify(exactly = 1) { client.requestPreAuthorizedTemporaryExposureKeyRelease() }
    }

    @Test
    fun `getTEKHistory throws ApiException on new Api`() = runBlockingTest {
        shouldThrow<ApiException> {
            coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
            val error = mockk<ApiException>().apply {
                every { status.hasResolution() } returns true
                every { printStackTrace(any<PrintWriter>()) } just Runs
            }
            every { client.requestPreAuthorizedTemporaryExposureKeyHistory() } answers {
                MockGMSTask.forErrorOnComplete(
                    error
                )
            }
            createInstance().getTEKHistory()
        }
    }

    @Test
    fun `getTEKHistory doesn't throw Exception on new Api`() = runBlockingTest {
        shouldNotThrow<Exception> {
            coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true

            every { client.requestPreAuthorizedTemporaryExposureKeyHistory() } answers {
                MockGMSTask.forErrorOnComplete(Exception())
            }
            createInstance().getTEKHistory()
        }
    }

    @Test
    fun `getTEKHistory request keys from new Api fallback to old Api in error`() = runBlockingTest {
        coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
        every { client.requestPreAuthorizedTemporaryExposureKeyHistory() } answers {
            MockGMSTask.forErrorOnComplete(
                Exception()
            )
        }
        createInstance().getTEKHistory()
        verify(exactly = 1) { client.temporaryExposureKeyHistory }
    }

    @Test
    fun `waiting more than 20 sec for keys on new Api logs TimeoutCancellationException`() =
        runBlockingTest {
            coEvery { enfVersion.isAtLeast(ENFVersion.V1_8) } returns true
            every { client.requestPreAuthorizedTemporaryExposureKeyHistory() } answers {
                MockGMSTask.forValueOnComplete(
                    null
                )
            }
            every { client.requestPreAuthorizedTemporaryExposureKeyRelease() } answers {
                MockGMSTask.forValueOnComplete(
                    null
                )
            }
            every { context.registerReceiver(any(), any()) } answers { mockk() }
            every { context.unregisterReceiver(any()) } just Runs
            createInstance().getTEKHistory()
            delay(25000)
        }
}
