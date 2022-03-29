package de.rki.coronawarnapp.familytest.core.notification

import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.util.device.ForegroundState
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Provider

internal class FamilyTestResultAvailableNotificationServiceTest : BaseTest() {
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var familyTestRepository: FamilyTestRepository
    @MockK(relaxed = true) lateinit var context: Context
    @MockK lateinit var foregroundState: ForegroundState
    @MockK(relaxed = true) lateinit var navDeepLinkBuilder: NavDeepLinkBuilder
    @MockK lateinit var pendingIntent: PendingIntent
    @MockK lateinit var navDeepLinkBuilderProvider: Provider<NavDeepLinkBuilder>

    private val familyTest1 = FamilyCoronaTest(
        personName = "Person 1",
        coronaTest = CoronaTest(
            identifier = "id-1",
            type = BaseCoronaTest.Type.PCR,
            registeredAt = Instant.EPOCH,
            registrationToken = "registrationToken",
            uiState = CoronaTest.UiState(
                isResultAvailableNotificationSent = false,
                hasResultChangeBadge = true
            )
        )
    )

    private val familyTest2 = FamilyCoronaTest(
        personName = "Person 1",
        coronaTest = CoronaTest(
            identifier = "id-2",
            type = BaseCoronaTest.Type.RAPID_ANTIGEN,
            registeredAt = Instant.EPOCH,
            registrationToken = "registrationToken",
            uiState = CoronaTest.UiState(
                isResultAvailableNotificationSent = true,
                hasResultChangeBadge = true
            )
        )
    )

    private val familyTest3 = FamilyCoronaTest(
        personName = "Person 1",
        coronaTest = CoronaTest(
            identifier = "id-3",
            type = BaseCoronaTest.Type.PCR,
            registeredAt = Instant.EPOCH,
            registrationToken = "registrationToken",
            uiState = CoronaTest.UiState(
                isResultAvailableNotificationSent = false,
                hasResultChangeBadge = false
            )
        )
    )

    private val familyTest4 = FamilyCoronaTest(
        personName = "Person 1",
        coronaTest = CoronaTest(
            identifier = "id-4",
            type = BaseCoronaTest.Type.RAPID_ANTIGEN,
            registeredAt = Instant.EPOCH,
            registrationToken = "registrationToken",
            uiState = CoronaTest.UiState(
                isResultAvailableNotificationSent = true,
                hasResultChangeBadge = true
            )
        )
    )

    private val familyTest5 = FamilyCoronaTest(
        personName = "Person 2",
        coronaTest = CoronaTest(
            identifier = "id-5",
            type = BaseCoronaTest.Type.PCR,
            registeredAt = Instant.EPOCH,
            registrationToken = "registrationToken",
            uiState = CoronaTest.UiState(
                isResultAvailableNotificationSent = false,
                hasResultChangeBadge = true
            )
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(CoronaWarnApplication)

        every { CoronaWarnApplication.getAppContext() } returns context
        every { navDeepLinkBuilderProvider.get() } returns navDeepLinkBuilder
        every { navDeepLinkBuilder.createPendingIntent() } returns pendingIntent

        every { notificationHelper.newBaseBuilder() } returns mockk(relaxed = true)
        every { familyTestRepository.familyTests } returns flowOf(
            setOf(
                familyTest1,
                familyTest2,
                familyTest3,
                familyTest4,
                familyTest5
            )
        )
        every { notificationHelper.sendNotification(any(), any()) } just Runs
        coEvery { familyTestRepository.markAsNotified(any(), any()) } just Runs
    }

    @Test
    fun testNotifications() = runBlockingTest {
        instance(this).setup()

        coVerify {
            notificationHelper.sendNotification(any(), any())
            familyTestRepository.markAsNotified("id-1", true)
            familyTestRepository.markAsNotified("id-5", true)
        }

        coVerify(exactly = 0) {
            familyTestRepository.markAsNotified("id-2", true)
            familyTestRepository.markAsNotified("id-3", true)
            familyTestRepository.markAsNotified("id-4", true)
        }
    }

    private fun instance(scope: CoroutineScope) = FamilyTestResultAvailableNotificationService(
        context = context,
        appScope = scope,
        notificationHelper = notificationHelper,
        navDeepLinkBuilderProvider = navDeepLinkBuilderProvider,
        familyTestRepository = familyTestRepository
    )
}
