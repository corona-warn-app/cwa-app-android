package de.rki.coronawarnapp.receiver

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import de.rki.coronawarnapp.nearby.modules.calculationtracker.Calculation
import de.rki.coronawarnapp.nearby.modules.calculationtracker.CalculationTracker
import de.rki.coronawarnapp.util.di.AppInjector
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verifySequence
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider

/**
 * ExposureStateUpdateReceiver test.
 */
class ExposureStateUpdateReceiverTest : BaseTest() {

    @MockK private lateinit var context: Context

    @MockK private lateinit var intent: Intent
    @MockK private lateinit var workManager: WorkManager
    @MockK private lateinit var calculationTracker: CalculationTracker
    private val scope = TestCoroutineScope()

    class TestApp : Application(), HasAndroidInjector {
        override fun androidInjector(): AndroidInjector<Any> {
            // NOOP
            return mockk()
        }
    }

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(WorkManager::class)
        every { intent.getStringExtra(ExposureNotificationClient.EXTRA_TOKEN) } returns "token"

        mockkObject(AppInjector)

        val application = mockk<TestApp>()
        every { context.applicationContext } returns application
        val broadcastReceiverInjector = AndroidInjector<Any> {
            it as ExposureStateUpdateReceiver
            it.calculationTracker = calculationTracker
            it.dispatcherProvider = TestDispatcherProvider
            it.scope = scope
        }
        every { application.androidInjector() } returns broadcastReceiverInjector

        every { WorkManager.getInstance(context) } returns workManager
        every { workManager.enqueue(any<WorkRequest>()) } answers { mockk() }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `updated state intent`() {
        every { intent.action } returns ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED
        ExposureStateUpdateReceiver().onReceive(context, intent)

        verifySequence {
            workManager.enqueue(any<WorkRequest>())
            calculationTracker.finishCalculation("token", Calculation.Result.UPDATED_STATE)
        }
    }

    @Test
    fun `no matches intent`() {
        every { intent.action } returns ExposureNotificationClient.ACTION_EXPOSURE_NOT_FOUND
        ExposureStateUpdateReceiver().onReceive(context, intent)

        verifySequence {
            calculationTracker.finishCalculation("token", Calculation.Result.NO_MATCHES)
        }
    }
}
