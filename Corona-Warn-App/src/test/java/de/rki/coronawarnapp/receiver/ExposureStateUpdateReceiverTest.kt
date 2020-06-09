package de.rki.coronawarnapp.receiver

import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ExposureStateUpdateReceiver test.
 */
class ExposureStateUpdateReceiverTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var intent: Intent

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(WorkManager::class)
        every { intent.action } returns ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED
        every { intent.getStringExtra(ExposureNotificationClient.EXTRA_TOKEN) } returns "token"
    }

    /**
     * Test ExposureStateUpdateReceiver.
     */
    @Test
    fun testExposureStateUpdateReceiver() {
        val wm = mockk<WorkManager>()
        every { WorkManager.getInstance(context) } returns wm
        every { wm.enqueue(any<WorkRequest>()) } answers { mockk() }

        ExposureStateUpdateReceiver().onReceive(context, intent)

        verify {
            wm.enqueue(any<WorkRequest>())
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
