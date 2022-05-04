package de.rki.coronawarnapp.coronatest.type.pcr.notification

import android.app.PendingIntent
import android.content.Context
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.util.SafeNavDeepLinkBuilder
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.notifications.NavDeepLinkBuilderFactory
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PCRTestResultAvailableNotificationServiceTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context
    @MockK lateinit var foregroundState: ForegroundState
    @MockK(relaxed = true) lateinit var navDeepLinkBuilder: SafeNavDeepLinkBuilder
    @MockK lateinit var pendingIntent: PendingIntent
    @MockK lateinit var navDeepLinkBuilderFactory: NavDeepLinkBuilderFactory
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var coronaTestRepository: CoronaTestRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(CoronaWarnApplication)

        every { CoronaWarnApplication.getAppContext() } returns context
        every { navDeepLinkBuilderFactory.create(any()) } returns navDeepLinkBuilder
        every { navDeepLinkBuilder.createPendingIntent() } returns pendingIntent

        every { notificationHelper.newBaseBuilder() } returns mockk(relaxed = true)
    }

    fun createInstance(scope: CoroutineScope = TestCoroutineScope()) = PCRTestResultAvailableNotificationService(
        context = context,
        foregroundState = foregroundState,
        navDeepLinkBuilderFactory = navDeepLinkBuilderFactory,
        notificationHelper = notificationHelper,
        coronaTestRepository = coronaTestRepository,
        appScope = scope,
    )

    @Test
    fun `test notification in foreground`() = runTest {
        coEvery { foregroundState.isInForeground } returns flow { emit(true) }

        createInstance().showTestResultAvailableNotification(mockk())

        verify(exactly = 0) { navDeepLinkBuilderFactory.create(any()) }
    }

    @Test
    fun `test notification in background`() = runTest {
        coEvery { foregroundState.isInForeground } returns flow { emit(false) }
        every {
            notificationHelper.sendNotification(
                notificationId = any(),
                notification = any()
            )
        } just Runs

        val instance = createInstance()
        val coronaTest = mockk<BaseCoronaTest>().apply {
            every { type } returns BaseCoronaTest.Type.PCR
            every { identifier } returns TestIdentifier()
        }
        instance.showTestResultAvailableNotification(coronaTest)

        verifyOrder {
            navDeepLinkBuilderFactory.create(any())
            context.getString(R.string.notification_headline_test_result_ready)
            context.getString(R.string.notification_body_test_result_ready)
            notificationHelper.sendNotification(
                notificationId = any(),
                notification = any()
            )
        }
    }
}
