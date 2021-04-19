package de.rki.coronawarnapp.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.device.ForegroundState
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Provider

class TestResultAvailableNotificationServiceTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context
    @MockK lateinit var foregroundState: ForegroundState
    @MockK(relaxed = true) lateinit var navDeepLinkBuilder: NavDeepLinkBuilder
    @MockK lateinit var pendingIntent: PendingIntent
    @MockK lateinit var navDeepLinkBuilderProvider: Provider<NavDeepLinkBuilder>
    @MockK lateinit var notificationManager: NotificationManager
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var cwaSettings: CWASettings

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(CoronaWarnApplication)

        every { CoronaWarnApplication.getAppContext() } returns context
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { navDeepLinkBuilderProvider.get() } returns navDeepLinkBuilder
        every { navDeepLinkBuilder.createPendingIntent() } returns pendingIntent
        every { cwaSettings.isNotificationsTestEnabled.value } returns true

        every { notificationHelper.newBaseBuilder() } returns mockk(relaxed = true)
    }

    fun createInstance() = PCRTestResultAvailableNotificationService(
        context = context,
        foregroundState = foregroundState,
        navDeepLinkBuilderProvider = navDeepLinkBuilderProvider,
        notificationHelper = notificationHelper,
        cwaSettings = cwaSettings
    )

    @Test
    fun `check destination`() {
        val negative = createInstance().getNotificationDestination(CoronaTestResult.PCR_NEGATIVE)
        negative shouldBe (R.id.submissionTestResultPendingFragment)

        val invalid = createInstance().getNotificationDestination(CoronaTestResult.PCR_INVALID)
        invalid shouldBe (R.id.submissionTestResultPendingFragment)

        val positive = createInstance().getNotificationDestination(CoronaTestResult.PCR_POSITIVE)
        positive shouldBe (R.id.submissionTestResultPendingFragment)
    }

    @Test
    fun `test notification in foreground`() = runBlockingTest {
        coEvery { foregroundState.isInForeground } returns flow { emit(true) }

        createInstance().showTestResultAvailableNotification(CoronaTestResult.PCR_POSITIVE)

        verify(exactly = 0) { navDeepLinkBuilderProvider.get() }
    }

    @Test
    fun `test notification in background`() = runBlockingTest {
        coEvery { foregroundState.isInForeground } returns flow { emit(false) }
        every {
            notificationHelper.sendNotification(
                notificationId = any(),
                notification = any()
            )
        } just Runs

        val instance = createInstance()

        instance.showTestResultAvailableNotification(CoronaTestResult.PCR_POSITIVE)

        verifyOrder {
            navDeepLinkBuilderProvider.get()
            instance.getNotificationDestination(CoronaTestResult.PCR_POSITIVE)
            context.getString(R.string.notification_headline_test_result_ready)
            context.getString(R.string.notification_body_test_result_ready)
            notificationHelper.sendNotification(
                notificationId = any(),
                notification = any()
            )
        }
    }

    @Test
    fun `test notification in background disabled`() = runBlockingTest {
        coEvery { foregroundState.isInForeground } returns flow { emit(false) }
        every { cwaSettings.isNotificationsTestEnabled.value } returns false

        createInstance().apply {
            showTestResultAvailableNotification(CoronaTestResult.PCR_POSITIVE)

            verify(exactly = 0) {
                notificationHelper.sendNotification(
                    notificationId = any(),
                    notification = any()
                )
            }
        }
    }
}
