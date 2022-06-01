package de.rki.coronawarnapp.presencetracing.checkins.checkout.auto

import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.presencetracing.checkins.checkout.CheckOutNotification
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AutoCheckOutWorkerTest : BaseTest() {

    @MockK lateinit var context: Context
    @RelaxedMockK lateinit var workerParams: WorkerParameters
    @MockK lateinit var autoCheckOut: AutoCheckOut
    @MockK lateinit var checkOutNotification: CheckOutNotification

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        autoCheckOut.apply {
            coEvery { performCheckOut(any()) } returns true
            coEvery { processOverDueCheckouts() } returns listOf(43, 44)
            coEvery { refreshAlarm() } returns true
        }

        coEvery { checkOutNotification.showAutoCheckoutNotification(any()) } just Runs
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createWorker() = AutoCheckOutWorker(
        context = context,
        workerParams = workerParams,
        autoCheckOut = autoCheckOut,
        checkOutNotification = checkOutNotification,
    )

    @Test
    fun `triggered via alarm`() = runTest {
        every { workerParams.inputData } returns Data.Builder().apply {
            putBoolean("autoCheckout.overdue", true)
            putLong("autoCheckout.checkInId", 42L)
        }.build()

        createWorker().doWork()

        coVerifySequence {
            autoCheckOut.performCheckOut(42)
            autoCheckOut.processOverDueCheckouts()
            autoCheckOut.refreshAlarm()

            checkOutNotification.showAutoCheckoutNotification(42)
        }
    }

    @Test
    fun `triggered on reboot or update`() = runTest {
        coEvery { autoCheckOut.processOverDueCheckouts() } returns emptyList()
        every { workerParams.inputData } returns Data.Builder().apply {
            putBoolean("autoCheckout.overdue", true)
        }.build()

        createWorker().doWork()

        coVerifySequence {
            autoCheckOut.processOverDueCheckouts()
            autoCheckOut.refreshAlarm()
        }

        coVerify(exactly = 0) { checkOutNotification.showAutoCheckoutNotification(any()) }
    }
}
