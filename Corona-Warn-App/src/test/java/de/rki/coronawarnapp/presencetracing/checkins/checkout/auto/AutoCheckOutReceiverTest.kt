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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AutoCheckOutReceiverTest : BaseTest() {

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
    fun `match our intent`() = runTest {
        every { intent.action } returns "de.rki.coronawarnapp.intent.action.AUTO_CHECKOUT"
        every { intent.getLongExtra("autoCheckout.checkInId", 0L) } returns 42L
        spyk(AutoCheckOutReceiver())
            .apply {
                every { goAsync() } returns mockk(relaxed = true)
                onReceive(context, intent)
            }
        advanceUntilIdle()

        verify { workManager.enqueue(any<WorkRequest>()) }

        workRequestSlot.captured.workSpec.input.getBoolean("autoCheckout.overdue", false) shouldBe true
        workRequestSlot.captured.workSpec.input.getLong("autoCheckout.checkInId", 0) shouldBe 42L
    }

    @Test
    fun `do not match unknown intents`() = runTest {
        every { intent.action } returns "yolo"
        spyk(AutoCheckOutReceiver())
            .apply {
                every { goAsync() } returns mockk(relaxed = true)
                onReceive(context, intent)
            }

        advanceUntilIdle()

        verify(exactly = 0) { workManager.enqueue(any<WorkRequest>()) }
    }
}
