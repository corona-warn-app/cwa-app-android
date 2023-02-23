package de.rki.coronawarnapp.eol

import android.app.AlarmManager
import android.app.PendingIntent
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.coronatest.notification.ShareTestResultNotification
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.modules.tracing.disableTracingIfEnabled
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOutIntentFactory
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import java.time.ZonedDateTime

class AppEolTest : BaseTest() {

    @MockK lateinit var enfClient: ENFClient
    @MockK lateinit var eolSetting: EolSetting
    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var debugLogger: DebugLogger
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var alarmManager: AlarmManager
    @MockK lateinit var appShortcutsHelper: AppShortcutsHelper
    @MockK lateinit var intentFactory: AutoCheckOutIntentFactory
    @MockK lateinit var notification: ShareTestResultNotification
    @MockK lateinit var notificationManager: NotificationManagerCompat

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("de.rki.coronawarnapp.nearby.modules.tracing.TracingStatusKt")

        coEvery { enfClient.disableTracingIfEnabled() } returns false
        coEvery { debugLogger.stop() } just Runs

        every { eolSetting.eolDateTime } returns flowOf(ZonedDateTime.parse("2023-06-01T00:00:00+02:00"))
        every { eolSetting.isLoggerAllowed } returns flowOf(false)
        every { alarmManager.cancel(any<PendingIntent>()) } just Runs
        every { workManager.cancelAllWork() } returns mockk()
        every { appShortcutsHelper.initShortcuts(any()) } returns mockk()
        every { intentFactory.createIntent(any()) } returns mockk()
        every { notification.cancelSharePositiveTestResultNotification(any(), any()) } just Runs
        every { notificationManager.cancelAll() } just Runs
        every { timeStamper.nowZonedDateTime } returns ZonedDateTime.parse("2023-02-01T00:00:00+02:00")
    }

    @Test
    fun `Eol is not reached yet`() = runTest2 {
        instance(this).isEol.first() shouldBe false
        coVerify(exactly = 0) {
            workManager.cancelAllWork()
            notificationManager.cancelAll()
            alarmManager.cancel(any<PendingIntent>())
            intentFactory.createIntent(any())
            notification.cancelSharePositiveTestResultNotification(any(), any())
            appShortcutsHelper.initShortcuts(any())
            debugLogger.stop()
            enfClient.disableTracingIfEnabled()
        }
    }

    @Test
    fun `Eol happened`() = runTest2 {
        every { timeStamper.nowZonedDateTime } returns ZonedDateTime.parse("2023-06-01T00:00:00+02:00")
        instance(this).isEol.first() shouldBe true
        coVerify {
            workManager.cancelAllWork()
            notificationManager.cancelAll()
            alarmManager.cancel(any<PendingIntent>())
            intentFactory.createIntent(any())
            notification.cancelSharePositiveTestResultNotification(any(), any())
            appShortcutsHelper.initShortcuts(any())
            debugLogger.stop()
            enfClient.disableTracingIfEnabled()
        }
    }

    @Test
    fun `Eol doesn't crash`() = runTest2 {
        coEvery { enfClient.disableTracingIfEnabled() } throws Exception("Crash!")
        shouldNotThrowAny {
            instance(this).isEol.first()
        }
    }

    private fun instance(scope: CoroutineScope) = AppEol(
        appScope = scope,
        enfClient = enfClient,
        eolSetting = eolSetting,
        timeStamper = timeStamper,
        debugLogger = debugLogger,
        workManager = workManager,
        notification = notification,
        alarmManager = alarmManager,
        intentFactory = intentFactory,
        appShortcutsHelper = appShortcutsHelper,
        notificationManager = notificationManager
    )
}
