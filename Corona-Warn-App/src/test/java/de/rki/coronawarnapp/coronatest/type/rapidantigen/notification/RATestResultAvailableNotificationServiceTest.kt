package de.rki.coronawarnapp.coronatest.type.rapidantigen.notification

import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.util.device.ForegroundState
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
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Provider

class RATestResultAvailableNotificationServiceTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context
    @MockK lateinit var foregroundState: ForegroundState
    @MockK(relaxed = true) lateinit var navDeepLinkBuilder: NavDeepLinkBuilder
    @MockK lateinit var pendingIntent: PendingIntent
    @MockK lateinit var navDeepLinkBuilderProvider: Provider<NavDeepLinkBuilder>
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var coronaTestRepository: CoronaTestRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(CoronaWarnApplication)

        every { CoronaWarnApplication.getAppContext() } returns context
        every { navDeepLinkBuilderProvider.get() } returns navDeepLinkBuilder
        every { navDeepLinkBuilder.createPendingIntent() } returns pendingIntent

        every { notificationHelper.newBaseBuilder() } returns mockk(relaxed = true)
    }

    fun createInstance(scope: CoroutineScope = TestCoroutineScope()) = RATTestResultAvailableNotificationService(
        context = context,
        foregroundState = foregroundState,
        navDeepLinkBuilderProvider = navDeepLinkBuilderProvider,
        notificationHelper = notificationHelper,
        personalTestRepository = coronaTestRepository,
        appScope = scope,
    )

    @Test
    fun `test notification in foreground`() = runBlockingTest {
        coEvery { foregroundState.isInForeground } returns flow { emit(true) }

        createInstance().showTestResultAvailableNotification(mockk())

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
        val coronaTest = mockk<BaseCoronaTest>().apply {
            every { type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
            every { identifier } returns TestIdentifier()
        }
        instance.showTestResultAvailableNotification(coronaTest)

        verifyOrder {
            navDeepLinkBuilderProvider.get()
            context.getString(R.string.notification_headline_test_result_ready)
            context.getString(R.string.notification_body_test_result_ready)
            notificationHelper.sendNotification(
                notificationId = any(),
                notification = any()
            )
        }
    }
}
