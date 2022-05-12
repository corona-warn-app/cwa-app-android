package de.rki.coronawarnapp.presencetracing.checkins.checkout.auto

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import androidx.work.WorkRequest
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import de.rki.coronawarnapp.util.di.AppInjector
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider

class AutoCheckOutBootRestoreReceiverTest : BaseTest() {

    @MockK private lateinit var context: Context

    @MockK private lateinit var intent: Intent
    @MockK private lateinit var workManager: WorkManager

    lateinit var workRequestSlot: CapturingSlot<WorkRequest>

    private val application = mockk<TestApp>()

    class TestApp : Application(), HasAndroidInjector {
        override fun androidInjector(): AndroidInjector<Any> {
            // NOOP
            return mockk()
        }
    }

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(AppInjector)

        every { context.applicationContext } returns application
        workRequestSlot = slot()
        every { workManager.enqueue(capture(workRequestSlot)) } answers { mockk() }
    }

    @Test
    fun `match boot intent`() = runTest(UnconfinedTestDispatcher()) {
        setup()
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
        setup()
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
        setup()
        every { intent.action } returns "yolo"
        AutoCheckOutBootRestoreReceiver().apply {
            onReceive(context, intent)
        }

        verify(exactly = 0) { workManager.enqueue(any<WorkRequest>()) }
    }

    private fun TestScope.setup() {
        val broadcastReceiverInjector = AndroidInjector<Any> {
            it as AutoCheckOutBootRestoreReceiver
            it.dispatcherProvider = TestDispatcherProvider()
            it.scope = this
            it.workManager = workManager
        }
        every { application.androidInjector() } returns broadcastReceiverInjector
    }
}
