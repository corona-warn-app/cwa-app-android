package de.rki.coronawarnapp.coronatest.notification

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
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
    private var numberOfRemainingSharePositiveTestResultReminders: Int = Int.MIN_VALUE

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { coronaTestRepository.coronaTests } returns coronaTestFlow
        every { cwaSettings.numberOfRemainingSharePositiveTestResultReminders = any() } answers {
            numberOfRemainingSharePositiveTestResultReminders = arg(0)
        }
        every { cwaSettings.numberOfRemainingSharePositiveTestResultReminders } answers {
            numberOfRemainingSharePositiveTestResultReminders
        }

        every { shareTestResultNotification.showSharePositiveTestResultNotification(any()) } just Runs
        every { shareTestResultNotification.cancelSharePositiveTestResultNotification() } just Runs
        every { shareTestResultNotification.scheduleSharePositiveTestResultReminder() } just Runs
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
                every { isSubmissionAllowed } returns true
                every { isSubmitted } returns false
            }
        )

        instance.setup()

        verify { shareTestResultNotification.scheduleSharePositiveTestResultReminder() }
        verify { cwaSettings.numberOfRemainingSharePositiveTestResultReminders = 2 }
    }

    @Test
    fun `showing a notification consumes a token`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)
        numberOfRemainingSharePositiveTestResultReminders = 2

        instance.maybeShowSharePositiveTestResultNotification(1)

        numberOfRemainingSharePositiveTestResultReminders shouldBe 1

        verify { shareTestResultNotification.showSharePositiveTestResultNotification(1) }
    }

    @Test
    fun `if there are no tokens left to show a notification, cancel the current one`() =
        runBlockingTest2(ignoreActive = true) {
            val instance = createInstance(this)
            numberOfRemainingSharePositiveTestResultReminders = 0

            instance.maybeShowSharePositiveTestResultNotification(1)

            numberOfRemainingSharePositiveTestResultReminders shouldBe 0

            verify { shareTestResultNotification.cancelSharePositiveTestResultNotification() }
        }

    @Test
    fun `any test which allowes submission triggers scheduling`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        coronaTestFlow.value = setOf(
            mockk<CoronaTest>().apply {
                every { isSubmissionAllowed } returns true
                every { isSubmitted } returns false
            }
        )

        instance.setup()

        verify { shareTestResultNotification.scheduleSharePositiveTestResultReminder() }
        verify { cwaSettings.numberOfRemainingSharePositiveTestResultReminders = 2 }
    }

    @Test
    fun `if there are no tests, we reset scheduling`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        coronaTestFlow.value = emptySet()

        instance.setup()

        advanceUntilIdle()

        verify { shareTestResultNotification.cancelSharePositiveTestResultNotification() }
        verify { cwaSettings.numberOfRemainingSharePositiveTestResultReminders = Int.MIN_VALUE }
    }
}
