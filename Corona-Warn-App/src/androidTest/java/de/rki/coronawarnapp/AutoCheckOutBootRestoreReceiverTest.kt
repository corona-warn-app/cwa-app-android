package de.rki.coronawarnapp

import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import androidx.work.WorkRequest
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOutBootRestoreReceiver
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTestInstrumentation

class AutoCheckOutBootRestoreReceiverTest : BaseTestInstrumentation() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var intent: Intent
    @MockK private lateinit var workManager: WorkManager

    private lateinit var workRequestSlot: CapturingSlot<WorkRequest>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        workRequestSlot = slot()
        every { workManager.enqueue(capture(workRequestSlot)) } answers { mockk() }
    }

    @Test
    fun matchBootIntent() = runTest(UnconfinedTestDispatcher()) {
        every { intent.action } returns Intent.ACTION_BOOT_COMPLETED
        spyk(AutoCheckOutBootRestoreReceiver()).apply {
            every { goAsync() } returns mockk(relaxed = true)
            onReceive(context, intent)
        }

        verify { workManager.enqueue(any<WorkRequest>()) }

        workRequestSlot.captured.workSpec.input.getBoolean("autoCheckout.overdue", false) shouldBe true
    }

    @Test
    fun matchAppUpdateIntent() = runTest(UnconfinedTestDispatcher()) {
        every { intent.action } returns Intent.ACTION_MY_PACKAGE_REPLACED
        spyk(AutoCheckOutBootRestoreReceiver()).apply {
            every { goAsync() } returns mockk(relaxed = true)
            onReceive(context, intent)
        }

        verify { workManager.enqueue(any<WorkRequest>()) }

        workRequestSlot.captured.workSpec.input.getBoolean("autoCheckout.overdue", false) shouldBe true
    }

    @Test
    fun doNotMatchUnknownIntents() = runTest(UnconfinedTestDispatcher()) {
        every { intent.action } returns "yolo"
        AutoCheckOutBootRestoreReceiver().apply {
            onReceive(context, intent)
        }

        verify(exactly = 0) { workManager.enqueue(any<WorkRequest>()) }
    }
}
