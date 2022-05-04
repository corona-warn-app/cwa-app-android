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
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class ShareTestResultNotificationServiceTest : BaseTest() {
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var shareTestResultNotification: ShareTestResultNotification

    private val coronaTestFlow = MutableStateFlow(
        emptySet<PersonalCoronaTest>()
    )
    private var numberOfRemainingSharePositiveTestResultRemindersPcr: Int = Int.MIN_VALUE
    private var numberOfRemainingSharePositiveTestResultRemindersRat: Int = Int.MIN_VALUE
    private var idOfPositiveTestResultRemindersPcr: TestIdentifier? = null
    private var idOfPositiveTestResultRemindersRat: TestIdentifier? = null

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { coronaTestRepository.coronaTests } returns coronaTestFlow
        every { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr = any() } answers {
            numberOfRemainingSharePositiveTestResultRemindersPcr = arg(0)
        }
        every { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr } answers {
            numberOfRemainingSharePositiveTestResultRemindersPcr
        }
        every { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat = any() } answers {
            numberOfRemainingSharePositiveTestResultRemindersRat = arg(0)
        }
        every { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat } answers {
            numberOfRemainingSharePositiveTestResultRemindersRat
        }
        every { cwaSettings.idOfPositiveTestResultRemindersPcr = any() } answers {
            idOfPositiveTestResultRemindersPcr = arg(0)
        }
        every { cwaSettings.idOfPositiveTestResultRemindersRat = any() } answers {
            idOfPositiveTestResultRemindersRat = arg(0)
        }
        every { cwaSettings.idOfPositiveTestResultRemindersPcr } answers {
            idOfPositiveTestResultRemindersPcr
        }
        every { cwaSettings.idOfPositiveTestResultRemindersRat } answers {
            idOfPositiveTestResultRemindersRat
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
    fun `any test which allows submission and is viewed triggers scheduling`() = runBlockingTest2(ignoreActive = true) {
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

        instance.setup()

        verify(exactly = 1) {
            shareTestResultNotification.scheduleSharePositiveTestResultReminder(
                PCR,
                PCR_ID,
                POSITIVE_PCR_RESULT_NOTIFICATION_ID
            )
        }
        verify(exactly = 1) {
            shareTestResultNotification.scheduleSharePositiveTestResultReminder(
                RAPID_ANTIGEN,
                RAT_ID,
                POSITIVE_RAT_RESULT_NOTIFICATION_ID
            )
        }

        verify { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr = 2 }
        verify { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat = 2 }
        verify { cwaSettings.idOfPositiveTestResultRemindersPcr = "PCR-ID" }
        verify { cwaSettings.idOfPositiveTestResultRemindersRat = "RAT-ID" }
    }

    @Test
    fun `no notification should be scheduled for tests that are not viewed yet`() =
        runBlockingTest2(ignoreActive = true) {
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

            instance.setup()
            verify(exactly = 0) {
                shareTestResultNotification.scheduleSharePositiveTestResultReminder(
                    PCR,
                    PCR_ID,
                    POSITIVE_PCR_RESULT_NOTIFICATION_ID
                )
            }
            verify(exactly = 0) {
                shareTestResultNotification.scheduleSharePositiveTestResultReminder(
                    RAPID_ANTIGEN,
                    RAT_ID,
                    POSITIVE_RAT_RESULT_NOTIFICATION_ID
                )
            }

            verify { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr = Int.MIN_VALUE }
            verify { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat = Int.MIN_VALUE }
        }

    @Test
    fun `showing a notification consumes a token`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)
        numberOfRemainingSharePositiveTestResultRemindersPcr = 2
        numberOfRemainingSharePositiveTestResultRemindersRat = 2

        instance.maybeShowSharePositiveTestResultNotification(1, PCR, PCR_ID)
        instance.maybeShowSharePositiveTestResultNotification(1, RAPID_ANTIGEN, RAT_ID)

        numberOfRemainingSharePositiveTestResultRemindersPcr shouldBe 1
        numberOfRemainingSharePositiveTestResultRemindersRat shouldBe 1

        verify { shareTestResultNotification.showSharePositiveTestResultNotification(1, PCR_ID) }
        verify { shareTestResultNotification.showSharePositiveTestResultNotification(1, RAT_ID) }
    }

    @Test
    fun `if there are no tokens left to show a notification, cancel the current one`() =
        runBlockingTest2(ignoreActive = true) {
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
    fun `reset notification if no test is stored or test was deleted`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        coronaTestFlow.value = emptySet()

        instance.setup()

        advanceUntilIdle()

        verify {
            shareTestResultNotification.cancelSharePositiveTestResultNotification(
                PCR,
                POSITIVE_PCR_RESULT_NOTIFICATION_ID
            )
        }
        verify {
            shareTestResultNotification.cancelSharePositiveTestResultNotification(
                RAPID_ANTIGEN,
                POSITIVE_RAT_RESULT_NOTIFICATION_ID
            )
        }
        verify { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr = Int.MIN_VALUE }
        verify { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat = Int.MIN_VALUE }
    }

    companion object {
        private const val PCR_ID = "PCR-ID"
        private const val RAT_ID = "RAT-ID"
    }
}
