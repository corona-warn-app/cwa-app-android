package de.rki.coronawarnapp

import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection
import de.rki.coronawarnapp.receiver.ExposureStateUpdateReceiver
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verifySequence
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTestInstrumentation

/**
 * ExposureStateUpdateReceiver test.
 */
class ExposureStateUpdateReceiverTest : BaseTestInstrumentation() {

    @MockK private lateinit var context: Context

    @MockK private lateinit var intent: Intent
    @MockK private lateinit var workManager: WorkManager
    @MockK private lateinit var exposureDetectionTracker: ExposureDetectionTracker

    private val scope = TestScope()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        @Suppress("DEPRECATION")
        every { intent.getStringExtra(ExposureNotificationClient.EXTRA_TOKEN) } returns "token"
        every { workManager.enqueue(any<WorkRequest>()) } answers { mockk() }
        every { exposureDetectionTracker.finishExposureDetection(any(), any()) } just Runs
    }

    @Test
    fun updatedStateIntent() {
        every { intent.action } returns ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED
        ExposureStateUpdateReceiver().onReceive(context, intent)

        scope.advanceUntilIdle()

        verifySequence {
            exposureDetectionTracker.finishExposureDetection(null, TrackedExposureDetection.Result.UPDATED_STATE)
            workManager.enqueue(any<WorkRequest>())
        }
    }

    @Test
    fun noMatchesIntent() {
        every { intent.action } returns ExposureNotificationClient.ACTION_EXPOSURE_NOT_FOUND
        ExposureStateUpdateReceiver().onReceive(context, intent)

        scope.advanceUntilIdle()

        verifySequence {
            exposureDetectionTracker.finishExposureDetection(null, TrackedExposureDetection.Result.NO_MATCHES)
            workManager.enqueue(any<WorkRequest>())
        }
    }
}
