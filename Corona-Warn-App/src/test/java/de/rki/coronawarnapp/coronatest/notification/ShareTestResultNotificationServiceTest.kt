package de.rki.coronawarnapp.coronatest.notification

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_PCR_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RAT_RESULT_NOTIFICATION_ID
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2

class ShareTestResultNotificationServiceTest : BaseTest() {
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var shareTestResultNotification: ShareTestResultNotification

    private val coronaTestFlow = MutableStateFlow(
        emptySet<PersonalCoronaTest>()
    )
    private var numberOfRemainingSharePositiveTestResultRemindersPcr: Int = Int.MIN_VALUE
    private var numberOfRemainingSharePositiveTestResultRemindersRat: Int = Int.MIN_VALUE
    private var idOfPositiveTestResultRemindersPcr: TestIdentifier = String()
    private var idOfPositiveTestResultRemindersRat: TestIdentifier = String()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { coronaTestRepository.coronaTests } returns coronaTestFlow
        coEvery { cwaSettings.updateNumberOfRemainingSharePositiveTestResultRemindersPcr(any()) } answers {
            numberOfRemainingSharePositiveTestResultRemindersPcr = arg(0)
        }
        every { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr } answers {
            flowOf(numberOfRemainingSharePositiveTestResultRemindersPcr)
        }
        coEvery { cwaSettings.updateNumberOfRemainingSharePositiveTestResultRemindersRat(any()) } answers {
            numberOfRemainingSharePositiveTestResultRemindersRat = arg(0)
        }
        every { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat } answers {
            flowOf(numberOfRemainingSharePositiveTestResultRemindersRat)
        }
        coEvery { cwaSettings.updateIdOfPositiveTestResultRemindersPcr(any()) } just Runs
        coEvery { cwaSettings.updateIdOfPositiveTestResultRemindersRat(any()) } just Runs
        every { cwaSettings.idOfPositiveTestResultRemindersPcr } answers {
            flowOf(idOfPositiveTestResultRemindersPcr)
        }
        every { cwaSettings.idOfPositiveTestResultRemindersRat } answers {
            flowOf(idOfPositiveTestResultRemindersRat)
        }

        every { shareTestResultNotification.showSharePositiveTestResultNotification(any(), any()) } just Runs
        every { shareTestResultNotification.cancelSharePositiveTestResultNotification(any(), any()) } just Runs
        every { shareTestResultNotification.scheduleSharePositiveTestResultReminder(any(), any(), any()) } just Runs
    }

    private fun createInstance(scope: CoroutineScope) = ShareTestResultNotificationService(
        appScope = scope,
        cwaSettings = cwaSettings,
        coronaTestRepository = coronaTestRepository,
        notification = shareTestResultNotification,
    )

    @Test
    fun `any test which allows submission and is viewed triggers scheduling`() = runTest2 {
        val instance = createInstance(this)

        coronaTestFlow.value = setOf(
            mockk<PersonalCoronaTest>().apply {
                every { identifier } returns "PCR-ID"
                every { type } returns PCR
                every { isSubmissionAllowed } returns true
                every { isSubmitted } returns false
                every { isViewed } returns true
            },
            mockk<PersonalCoronaTest>().apply {
                every { identifier } returns "RAT-ID"
                every { type } returns RAPID_ANTIGEN
                every { isSubmissionAllowed } returns true
                every { isSubmitted } returns false
                every { isViewed } returns true
            }
        )

        instance.initialize()

        verify(exactly = 1) {
            shareTestResultNotification.scheduleSharePositiveTestResultReminder(
                PCR,
                PCR_ID,
                POSITIVE_PCR_RESULT_NOTIFICATION_ID
            )
            shareTestResultNotification.scheduleSharePositiveTestResultReminder(
                RAPID_ANTIGEN,
                RAT_ID,
                POSITIVE_RAT_RESULT_NOTIFICATION_ID
            )
        }

        coVerify {
            cwaSettings.updateNumberOfRemainingSharePositiveTestResultRemindersPcr(2)
            cwaSettings.updateNumberOfRemainingSharePositiveTestResultRemindersRat(2)
            cwaSettings.updateIdOfPositiveTestResultRemindersPcr("PCR-ID")
            cwaSettings.updateIdOfPositiveTestResultRemindersRat("RAT-ID")
        }
    }

    @Test
    fun `no notification should be scheduled for tests that are not viewed yet`() =
        runTest2 {
            val instance = createInstance(this)

            coronaTestFlow.value = setOf(
                mockk<PersonalCoronaTest>().apply {
                    every { identifier } returns PCR_ID
                    every { type } returns PCR
                    every { isSubmissionAllowed } returns true
                    every { isSubmitted } returns false
                    every { isViewed } returns false
                },
                mockk<PersonalCoronaTest>().apply {
                    every { identifier } returns RAT_ID
                    every { type } returns RAPID_ANTIGEN
                    every { isSubmissionAllowed } returns true
                    every { isSubmitted } returns false
                    every { isViewed } returns false
                }
            )

            instance.initialize()
            verify(exactly = 0) {
                shareTestResultNotification.scheduleSharePositiveTestResultReminder(
                    PCR,
                    PCR_ID,
                    POSITIVE_PCR_RESULT_NOTIFICATION_ID
                )
                shareTestResultNotification.scheduleSharePositiveTestResultReminder(
                    RAPID_ANTIGEN,
                    RAT_ID,
                    POSITIVE_RAT_RESULT_NOTIFICATION_ID
                )
            }

            coVerify {
                cwaSettings.updateNumberOfRemainingSharePositiveTestResultRemindersPcr(Int.MIN_VALUE)
                cwaSettings.updateNumberOfRemainingSharePositiveTestResultRemindersRat(Int.MIN_VALUE)
            }
        }

    @Test
    fun `showing a notification consumes a token`() = runTest2 {
        val instance = createInstance(this)
        numberOfRemainingSharePositiveTestResultRemindersPcr = 2
        numberOfRemainingSharePositiveTestResultRemindersRat = 2

        instance.maybeShowSharePositiveTestResultNotification(1, PCR, PCR_ID)
        instance.maybeShowSharePositiveTestResultNotification(1, RAPID_ANTIGEN, RAT_ID)

        numberOfRemainingSharePositiveTestResultRemindersPcr shouldBe 1
        numberOfRemainingSharePositiveTestResultRemindersRat shouldBe 1

        verify {
            shareTestResultNotification.showSharePositiveTestResultNotification(1, PCR_ID)
            shareTestResultNotification.showSharePositiveTestResultNotification(1, RAT_ID)
        }
    }

    @Test
    fun `if there are no tokens left to show a notification, cancel the current one`() = runTest2 {
        val instance = createInstance(this)

        // PCR
        numberOfRemainingSharePositiveTestResultRemindersPcr = 0
        instance.maybeShowSharePositiveTestResultNotification(1, PCR, PCR_ID)
        numberOfRemainingSharePositiveTestResultRemindersPcr shouldBe 0
        verify {
            shareTestResultNotification.cancelSharePositiveTestResultNotification(
                PCR,
                POSITIVE_PCR_RESULT_NOTIFICATION_ID
            )
        }

        // RAT
        numberOfRemainingSharePositiveTestResultRemindersRat = 0
        instance.maybeShowSharePositiveTestResultNotification(1, RAPID_ANTIGEN, RAT_ID)
        numberOfRemainingSharePositiveTestResultRemindersRat shouldBe 0
        verify {
            shareTestResultNotification.cancelSharePositiveTestResultNotification(
                RAPID_ANTIGEN,
                POSITIVE_RAT_RESULT_NOTIFICATION_ID
            )
        }
    }

    @Test
    fun `reset notification if no test is stored or test was deleted`() = runTest2 {
        val instance = createInstance(this)
        coronaTestFlow.value = emptySet()
        instance.initialize()
        advanceUntilIdle()

        verify {
            shareTestResultNotification.cancelSharePositiveTestResultNotification(
                PCR,
                POSITIVE_PCR_RESULT_NOTIFICATION_ID
            )
            shareTestResultNotification.cancelSharePositiveTestResultNotification(
                RAPID_ANTIGEN,
                POSITIVE_RAT_RESULT_NOTIFICATION_ID
            )
        }

        coVerify {
            cwaSettings.updateNumberOfRemainingSharePositiveTestResultRemindersPcr(Int.MIN_VALUE)
            cwaSettings.updateNumberOfRemainingSharePositiveTestResultRemindersRat(Int.MIN_VALUE)
        }
    }

    companion object {
        private const val PCR_ID = "PCR-ID"
        private const val RAT_ID = "RAT-ID"
    }
}
