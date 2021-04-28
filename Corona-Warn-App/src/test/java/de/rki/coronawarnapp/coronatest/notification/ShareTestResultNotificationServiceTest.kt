package de.rki.coronawarnapp.coronatest.notification

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.main.CWASettings
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class ShareTestResultNotificationServiceTest : BaseTest() {
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var shareTestResultNotification: ShareTestResultNotification

    private val coronaTestFlow = MutableStateFlow(
        emptySet<CoronaTest>()
    )
    private var numberOfRemainingSharePositiveTestResultRemindersPcr: Int = Int.MIN_VALUE
    private var numberOfRemainingSharePositiveTestResultRemindersRat: Int = Int.MIN_VALUE

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

        every { shareTestResultNotification.showSharePositiveTestResultNotification(any(), any()) } just Runs
        every { shareTestResultNotification.cancelSharePositiveTestResultNotification(any()) } just Runs
        every { shareTestResultNotification.scheduleSharePositiveTestResultReminder(any()) } just Runs
    }

    private fun createInstance(scope: CoroutineScope) = ShareTestResultNotificationService(
        appScope = scope,
        cwaSettings = cwaSettings,
        coronaTestRepository = coronaTestRepository,
        notification = shareTestResultNotification,
    )

    @Test
    fun `any test which allows submission triggers scheduling`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        coronaTestFlow.value = setOf(
            mockk<CoronaTest>().apply {
                every { type } returns PCR
                every { isSubmissionAllowed } returns true
                every { isSubmitted } returns false
            },
            mockk<CoronaTest>().apply {
                every { type } returns RAPID_ANTIGEN
                every { isSubmissionAllowed } returns true
                every { isSubmitted } returns false
            }
        )

        instance.setup()

        verify(exactly = 1) { shareTestResultNotification.scheduleSharePositiveTestResultReminder(PCR) }
        verify(exactly = 1) { shareTestResultNotification.scheduleSharePositiveTestResultReminder(RAPID_ANTIGEN) }

        verify { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr = 2 }
        verify { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat = 2 }
    }

    @Test
    fun `showing a notification consumes a token`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)
        numberOfRemainingSharePositiveTestResultRemindersPcr = 2
        numberOfRemainingSharePositiveTestResultRemindersRat = 2

        instance.maybeShowSharePositiveTestResultNotification(1, PCR)
        instance.maybeShowSharePositiveTestResultNotification(1, RAPID_ANTIGEN)

        numberOfRemainingSharePositiveTestResultRemindersPcr shouldBe 1
        numberOfRemainingSharePositiveTestResultRemindersRat shouldBe 1

        verify { shareTestResultNotification.showSharePositiveTestResultNotification(1, PCR) }
        verify { shareTestResultNotification.showSharePositiveTestResultNotification(1, RAPID_ANTIGEN) }
    }

    @Test
    fun `if there are no tokens left to show a notification, cancel the current one`() =
        runBlockingTest2(ignoreActive = true) {
            val instance = createInstance(this)

            // PCR
            numberOfRemainingSharePositiveTestResultRemindersPcr = 0
            instance.maybeShowSharePositiveTestResultNotification(1, PCR)
            numberOfRemainingSharePositiveTestResultRemindersPcr shouldBe 0
            verify { shareTestResultNotification.cancelSharePositiveTestResultNotification(PCR) }

            // RAT
            numberOfRemainingSharePositiveTestResultRemindersRat = 0
            instance.maybeShowSharePositiveTestResultNotification(1, RAPID_ANTIGEN)
            numberOfRemainingSharePositiveTestResultRemindersRat shouldBe 0
            verify { shareTestResultNotification.cancelSharePositiveTestResultNotification(PCR) }
        }

    @Test
    fun `reset notification if no test is stored or test was deleted`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        coronaTestFlow.value = emptySet()

        instance.setup()

        advanceUntilIdle()

        verify { shareTestResultNotification.cancelSharePositiveTestResultNotification(PCR) }
        verify { shareTestResultNotification.cancelSharePositiveTestResultNotification(RAPID_ANTIGEN) }
        verify { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr = Int.MIN_VALUE }
        verify { cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat = Int.MIN_VALUE }
    }
}
