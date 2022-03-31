package de.rki.coronawarnapp.familytest.core.notification

import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.notification.GeneralNotifications
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
import javax.inject.Provider

internal class FamilyTestResultNotificationServiceTest : BaseTest() {
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK(relaxed = true) lateinit var context: Context
    @MockK(relaxed = true) lateinit var navDeepLinkBuilder: NavDeepLinkBuilder
    @MockK lateinit var pendingIntent: PendingIntent
    @MockK lateinit var navDeepLinkBuilderProvider: Provider<NavDeepLinkBuilder>

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(CoronaWarnApplication)
        every { CoronaWarnApplication.getAppContext() } returns context
        every { navDeepLinkBuilderProvider.get() } returns navDeepLinkBuilder
        every { navDeepLinkBuilder.createPendingIntent() } returns pendingIntent
        every { notificationHelper.newBaseBuilder() } returns mockk(relaxed = true)
        every { notificationHelper.sendNotification(any(), any()) } just Runs
    }

    @Test
    fun testNotifications() = runBlockingTest {
        instance().showTestResultNotification()
        verify { notificationHelper.sendNotification(any(), any()) }
    }

    private fun instance() = FamilyTestNotificationService(
        context = context,
        notificationHelper = notificationHelper,
        navDeepLinkBuilderProvider = navDeepLinkBuilderProvider,
    )
}
