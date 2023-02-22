package de.rki.coronawarnapp.presencetracing.checkins.checkout.auto

import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import androidx.work.WorkRequest
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AutoCheckOutBootRestoreReceiverTest : BaseTest() {

    @MockK private lateinit var context: Context

    @MockK private lateinit var intent: Intent
    @MockK private lateinit var workManager: WorkManager

    private lateinit var workRequestSlot: CapturingSlot<WorkRequest>

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        workRequestSlot = slot()
        every { workManager.enqueue(capture(workRequestSlot)) } answers { mockk() }
    }

    @Test
    fun `match boot intent`() = runTest(UnconfinedTestDispatcher()) {
        every { intent.action } returns Intent.ACTION_BOOT_COMPLETED
        spyk(AutoCheckOutBootRestoreReceiver()).apply {
            every { goAsync() } returns mockk(relaxed = true)
            onReceive(context, intent)
        }

        verify { workManager.enqueue(any<WorkRequest>()) }

        workRequestSlot.captured.workSpec.input.getBoolean("autoCheckout.overdue", false) shouldBe true
    }

    @Test
    fun `match app update intent`() = runTest(UnconfinedTestDispatcher()) {
        every { intent.action } returns Intent.ACTION_MY_PACKAGE_REPLACED
        spyk(AutoCheckOutBootRestoreReceiver()).apply {
            every { goAsync() } returns mockk(relaxed = true)
            onReceive(context, intent)
        }

        verify { workManager.enqueue(any<WorkRequest>()) }

        workRequestSlot.captured.workSpec.input.getBoolean("autoCheckout.overdue", false) shouldBe true
    }

    @Test
    fun `do not match unknown intents`() = runTest(UnconfinedTestDispatcher()) {
        every { intent.action } returns "yolo"
        AutoCheckOutBootRestoreReceiver().apply {
            onReceive(context, intent)
        }

        verify(exactly = 0) { workManager.enqueue(any<WorkRequest>()) }
    }
}
