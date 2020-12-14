package de.rki.coronawarnapp.submission.data.tekhistory

import android.app.Activity
import android.content.Intent
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.TracingPermissionHelper
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TEKHistoryUpdaterTest : BaseTest() {
    @MockK lateinit var tekHistoryStorage: TEKHistoryStorage
    @MockK lateinit var tracingPermissionHelper: TracingPermissionHelper
    @MockK lateinit var tracingPermissionHelperFactory: TracingPermissionHelper.Factory
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var enfClient: ENFClient

    private val availableTEKs: List<TemporaryExposureKey> = listOf(mockk())

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.EPOCH

        coEvery { enfClient.getTEKHistoryOrRequestPermission(any(), any()) } just Runs
        coEvery { enfClient.isTracingEnabled } returns flowOf(true)
        coEvery { enfClient.getTEKHistory() } returns availableTEKs

        coEvery { tekHistoryStorage.storeTEKData(any()) } just Runs

        every { tracingPermissionHelperFactory.create(any()) } returns tracingPermissionHelper
        coEvery { tracingPermissionHelper.startTracing() } just Runs
        every { tracingPermissionHelper.handleActivityResult(any(), any(), any()) } returns false
    }

    fun createInstance(scope: CoroutineScope, callback: TEKHistoryUpdater.Callback) = TEKHistoryUpdater(
        callback = callback,
        scope = scope,
        tracingPermissionHelperFactory = tracingPermissionHelperFactory,
        tekHistoryStorage = tekHistoryStorage,
        timeStamper = timeStamper,
        enfClient = enfClient
    )

    @Test
    fun `request is forwaded to enf client`() = runBlockingTest {
        val callback = mockk<TEKHistoryUpdater.Callback>()
        val instance = createInstance(scope = this, callback = callback)

        instance.updateTEKHistoryOrRequestPermission()
        coVerify {
            enfClient.getTEKHistoryOrRequestPermission(
                any(),
                any()
            )
        }
    }

    @Test
    fun `if tracing is disabled then start tracing`() = runBlockingTest {
        coEvery { enfClient.isTracingEnabled } returns flowOf(false)

        every { tracingPermissionHelperFactory.create(any()) } returns tracingPermissionHelper

        val callback = mockk<TEKHistoryUpdater.Callback>()
        val instance = createInstance(scope = this, callback = callback)

        instance.updateTEKHistoryOrRequestPermission()

        verify {
            tracingPermissionHelper.startTracing()
        }
    }

    @Test
    fun `tracing callbacks are forwarded via tek updater callbacks`() = runBlockingTest {
        coEvery { enfClient.isTracingEnabled } returns flowOf(false)

        var tracingCallback: TracingPermissionHelper.Callback? = null
        every { tracingPermissionHelperFactory.create(any()) } answers {
            tracingCallback = arg(0)
            tracingPermissionHelper
        }

        val tekUpdaterCallback = mockk<TEKHistoryUpdater.Callback>(relaxUnitFun = true)
        val instance = createInstance(scope = this, callback = tekUpdaterCallback)

        instance.updateTEKHistoryOrRequestPermission()
        tracingCallback shouldNotBe null

        val consentRequest: (Boolean) -> Unit = { }
        tracingCallback!!.onTracingConsentRequired(consentRequest)

        val permissionRequest: (Activity) -> Unit = { }
        tracingCallback!!.onPermissionRequired(permissionRequest)

        verify {
            tracingPermissionHelper.startTracing()
            tekUpdaterCallback.onTracingConsentRequired(consentRequest)
            tekUpdaterCallback.onPermissionRequired(permissionRequest)
        }
    }

    @Test
    fun `tracing permission results are forwarded to the tracing permissionhelper`() = runBlockingTest {
        every { tracingPermissionHelper.handleActivityResult(any(), any(), any()) } returns true
        val callback = mockk<TEKHistoryUpdater.Callback>(relaxUnitFun = true)
        val instance = createInstance(scope = this, callback = callback)

        val testIntent = mockk<Intent>()
        instance.handleActivityResult(
            requestCode = TracingPermissionHelper.TRACING_PERMISSION_REQUESTCODE,
            resultCode = Activity.RESULT_OK,
            data = testIntent
        )

        verify {
            tracingPermissionHelper.handleActivityResult(
                requestCode = TracingPermissionHelper.TRACING_PERMISSION_REQUESTCODE,
                resultCode = Activity.RESULT_OK,
                data = testIntent
            )
        }
    }

    @Test
    fun `TEK activity results processed if not consumed by the tracing permissionhelper`() = runBlockingTest {
        every { tracingPermissionHelper.handleActivityResult(any(), any(), any()) } returns false
        val callback = mockk<TEKHistoryUpdater.Callback>(relaxUnitFun = true)
        val instance = createInstance(scope = this, callback = callback)

        val testIntent = mockk<Intent>()
        instance.handleActivityResult(
            requestCode = TEKHistoryUpdater.TEK_PERMISSION_REQUEST,
            resultCode = Activity.RESULT_CANCELED,
            data = testIntent
        ) shouldBe true

        verifySequence {
            tracingPermissionHelper.handleActivityResult(
                requestCode = TEKHistoryUpdater.TEK_PERMISSION_REQUEST,
                resultCode = Activity.RESULT_CANCELED,
                data = testIntent
            )
            callback.onTEKPermissionDeclined()
        }
    }

    @Test
    fun `unknown resultcodes are not consumed`() = runBlockingTest {
        every { tracingPermissionHelper.handleActivityResult(any(), any(), any()) } returns false
        val callback = mockk<TEKHistoryUpdater.Callback>(relaxUnitFun = true)
        val instance = createInstance(scope = this, callback = callback)

        val testIntent = mockk<Intent>()
        instance.handleActivityResult(
            requestCode = 123,
            resultCode = Activity.RESULT_OK,
            data = testIntent
        ) shouldBe false

        verify {
            tracingPermissionHelper.handleActivityResult(
                requestCode = 123,
                resultCode = Activity.RESULT_OK,
                data = testIntent
            )
        }
    }

    @Test
    fun `positive TEK activity results trigger new update attempt`() = runBlockingTest {
        every { tracingPermissionHelper.handleActivityResult(any(), any(), any()) } returns false
        val callback = mockk<TEKHistoryUpdater.Callback>(relaxUnitFun = true)
        val instance = createInstance(scope = this, callback = callback)

        val testIntent = mockk<Intent>()
        instance.handleActivityResult(
            requestCode = TEKHistoryUpdater.TEK_PERMISSION_REQUEST,
            resultCode = Activity.RESULT_OK,
            data = testIntent
        ) shouldBe true

        coVerifySequence {
            tracingPermissionHelper.handleActivityResult(
                requestCode = TEKHistoryUpdater.TEK_PERMISSION_REQUEST,
                resultCode = Activity.RESULT_OK,
                data = testIntent
            )
            enfClient.getTEKHistory()
            callback.onTEKAvailable(availableTEKs)
        }
    }
}
