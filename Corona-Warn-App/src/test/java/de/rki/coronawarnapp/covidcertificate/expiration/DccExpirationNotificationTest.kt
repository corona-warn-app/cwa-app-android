package de.rki.coronawarnapp.covidcertificate.expiration

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.covidcertificate.common.notification.DigitalCovidCertificateNotifications
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.notifications.NavDeepLinkBuilderFactory
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccExpirationNotificationTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context
    @MockK lateinit var foregroundState: ForegroundState
    @MockK(relaxed = true) lateinit var navDeepLinkBuilder: NavDeepLinkBuilder
    @MockK lateinit var pendingIntent: PendingIntent
    @MockK lateinit var deepLinkBuilderFactory: NavDeepLinkBuilderFactory
    @MockK lateinit var notificationManager: NotificationManager
    @MockK lateinit var notificationHelper: DigitalCovidCertificateNotifications

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(CoronaWarnApplication)

        every { CoronaWarnApplication.getAppContext() } returns context
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { navDeepLinkBuilder.createPendingIntent() } returns pendingIntent
        every { deepLinkBuilderFactory.create(any()) } returns navDeepLinkBuilder
        every { notificationHelper.newBaseBuilder() } returns mockk(relaxed = true)
        every {
            notificationHelper.sendNotification(
                notificationId = any(),
                notification = any()
            )
        } just Runs
    }

    fun createInstance() = DccExpirationNotification(
        context = context,
        deepLinkBuilderFactory = deepLinkBuilderFactory,
        notificationHelper = notificationHelper,
    )

    @Test
    fun `show expires soon notification for vaccination`() = runBlockingTest {
        createInstance().showNotification(VaccinationCertificateContainerId("the vax-scene"))
        verify { notificationHelper.sendNotification(any(), any()) }
    }

    @Test
    fun `show expired notification for vaccination`() = runBlockingTest {
        createInstance().showNotification(VaccinationCertificateContainerId("the vax-scene"))
        verify { notificationHelper.sendNotification(any(), any()) }
    }

    @Test
    fun `show expires soon notification for recovery`() = runBlockingTest {
        createInstance().showNotification(RecoveryCertificateContainerId("recovery"))
        verify { notificationHelper.sendNotification(any(), any()) }
    }

    @Test
    fun `show expired notification for recovery`() = runBlockingTest {
        createInstance().showNotification(RecoveryCertificateContainerId("recovery"))
        verify { notificationHelper.sendNotification(any(), any()) }
    }
}
