package de.rki.coronawarnapp.covidcertificate.booster

import android.app.PendingIntent
import android.content.Context
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.notification.DigitalCovidCertificateNotifications
import de.rki.coronawarnapp.covidcertificate.notification.PersonNotificationSender
import de.rki.coronawarnapp.util.SafeNavDeepLinkBuilder
import de.rki.coronawarnapp.util.notifications.NavDeepLinkBuilderFactory
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PersonNotificationSenderTest : BaseTest() {
    @MockK(relaxed = true) lateinit var context: Context
    @MockK(relaxed = true) lateinit var navDeepLinkBuilder: SafeNavDeepLinkBuilder
    @MockK lateinit var pendingIntent: PendingIntent
    @MockK lateinit var deepLinkBuilderFactory: NavDeepLinkBuilderFactory
    @MockK lateinit var notificationHelper: DigitalCovidCertificateNotifications

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(CoronaWarnApplication)

        every { CoronaWarnApplication.getAppContext() } returns context
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

    fun createInstance() = PersonNotificationSender(
        context = context,
        deepLinkBuilderFactory = deepLinkBuilderFactory,
        notificationHelper = notificationHelper,
    )

    @Test
    fun `show booster notification`() = runTest {
        val personIdentifier = CertificatePersonIdentifier(
            dateOfBirthFormatted = "1990-10-10",
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized"
        )
        createInstance().showNotification(personIdentifier)
        verify { notificationHelper.sendNotification(any(), any()) }
    }
}
