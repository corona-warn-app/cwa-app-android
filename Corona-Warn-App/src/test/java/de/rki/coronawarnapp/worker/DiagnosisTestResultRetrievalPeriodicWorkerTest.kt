package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.execution.TestResultScheduler
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.worker.PCRTestResultRetrievalWorker
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.util.TimeAndDateExtensions.daysToMilliseconds
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.util.encryptionmigration.EncryptedPreferencesFactory
import de.rki.coronawarnapp.util.encryptionmigration.EncryptionErrorResetTool
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTest

class DiagnosisTestResultRetrievalPeriodicWorkerTest : BaseTest() {
    @MockK lateinit var context: Context
    @MockK lateinit var request: WorkRequest
    @MockK lateinit var testResultAvailableNotificationService: PCRTestResultAvailableNotificationService
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var appComponent: ApplicationComponent
    @MockK lateinit var encryptedPreferencesFactory: EncryptedPreferencesFactory
    @MockK lateinit var encryptionErrorResetTool: EncryptionErrorResetTool
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var testResultScheduler: TestResultScheduler

    @RelaxedMockK lateinit var workerParams: WorkerParameters
    private val currentInstant = Instant.ofEpochSecond(1611764225)
    private val testToken = "test token"

    private val coronaTestFlow = MutableStateFlow(emptySet<CoronaTest>())

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns currentInstant

        mockkObject(AppInjector)
        every { AppInjector.component } returns appComponent
        every { appComponent.encryptedPreferencesFactory } returns encryptedPreferencesFactory
        every { appComponent.errorResetTool } returns encryptionErrorResetTool

        every { testResultScheduler.setPcrPeriodicTestPollingEnabled(enabled = any()) } just Runs

        every { notificationHelper.cancelCurrentNotification(any()) } just Runs

        coronaTestRepository.apply {
            every { coronaTests } answers { coronaTestFlow }
            coEvery { refresh(any()) } coAnswers { coronaTestFlow.first() }
            coEvery { updateResultNotification(identifier = any(), sent = any()) } just Runs
        }
    }

    private fun newCoronaTest(
        registered: Instant = currentInstant,
        viewed: Boolean = false,
        result: CoronaTestResult = CoronaTestResult.PCR_POSITIVE,
        isNotificationSent: Boolean = false,
    ): CoronaTest {
        return mockk<PCRCoronaTest>().apply {
            every { identifier } returns ""
            every { type } returns CoronaTest.Type.PCR
            every { registeredAt } returns registered
            every { isViewed } returns viewed
            every { testResult } returns result
            every { registrationToken } returns testToken
            every { isResultAvailableNotificationSent } returns isNotificationSent
        }
    }

    private fun createWorker() = PCRTestResultRetrievalWorker(
        context = context,
        workerParams = workerParams,
        testResultAvailableNotificationService = testResultAvailableNotificationService,
        notificationHelper = notificationHelper,
        coronaTestRepository = coronaTestRepository,
        testResultScheduler = testResultScheduler,
        timeStamper = timeStamper,
    )

    @Test
    fun testStopWorkerWhenResultHasBeenViewed() = runBlockingTest {
        coronaTestFlow.value = setOf(newCoronaTest(viewed = true))

        val result = createWorker().doWork()

        coVerify(exactly = 0) { coronaTestRepository.refresh(type = CoronaTest.Type.PCR) }
        verify(exactly = 1) { testResultScheduler.setPcrPeriodicTestPollingEnabled(enabled = false) }
        result shouldBe ListenableWorker.Result.success()
    }

    @Test
    fun testStopWorkerWhenNotificationSent() = runBlockingTest {
        coronaTestFlow.value = setOf(newCoronaTest(isNotificationSent = true))

        val result = createWorker().doWork()

        coVerify(exactly = 0) { coronaTestRepository.refresh(type = CoronaTest.Type.PCR) }
        verify(exactly = 1) { testResultScheduler.setPcrPeriodicTestPollingEnabled(enabled = false) }
        result shouldBe ListenableWorker.Result.success()
    }

    @Test
    fun testStopWorkerWhenMaxDaysExceeded() = runBlockingTest {
        val past =
            currentInstant - (BackgroundConstants.POLLING_VALIDITY_MAX_DAYS.toLong() + 1).daysToMilliseconds()
        coronaTestFlow.value = setOf(newCoronaTest(registered = past))

        val result = createWorker().doWork()

        coVerify(exactly = 0) { coronaTestRepository.refresh(type = CoronaTest.Type.PCR) }
        verify(exactly = 1) { testResultScheduler.setPcrPeriodicTestPollingEnabled(enabled = false) }
        result shouldBe ListenableWorker.Result.success()
    }

    @Test
    fun testSendNotificationWhenPositive() = runBlockingTest {
        val testResult = CoronaTestResult.PCR_POSITIVE
        coronaTestFlow.value = setOf(newCoronaTest(result = testResult))

        coEvery { testResultAvailableNotificationService.showTestResultAvailableNotification(testResult) } just Runs
        coEvery {
            notificationHelper.cancelCurrentNotification(
                NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
            )
        } just Runs

        val result = createWorker().doWork()

        coVerify {
            coronaTestRepository.refresh(type = CoronaTest.Type.PCR)
            testResultAvailableNotificationService.showTestResultAvailableNotification(testResult)
            notificationHelper.cancelCurrentNotification(
                NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
            )
            coronaTestRepository.updateResultNotification(any(), sent = true)
        }

        result shouldBe ListenableWorker.Result.success()
    }

    @Test
    fun testSendNotificationWhenNegative() = runBlockingTest {
        val testResult = CoronaTestResult.PCR_NEGATIVE
        coronaTestFlow.value = setOf(newCoronaTest(result = testResult))
        coEvery { testResultAvailableNotificationService.showTestResultAvailableNotification(testResult) } just Runs
        coEvery {
            notificationHelper.cancelCurrentNotification(
                NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
            )
        } just Runs

        val result = createWorker().doWork()

        coVerify {
            coronaTestRepository.refresh(type = CoronaTest.Type.PCR)
            testResultAvailableNotificationService.showTestResultAvailableNotification(testResult)
            notificationHelper.cancelCurrentNotification(
                NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
            )
            coronaTestRepository.updateResultNotification(any(), sent = true)
        }

        result shouldBe ListenableWorker.Result.success()
    }

    @Test
    fun testSendNotificationWhenInvalid() = runBlockingTest {
        val testResult = CoronaTestResult.PCR_INVALID
        coronaTestFlow.value = setOf(newCoronaTest(result = testResult))
        coEvery { testResultAvailableNotificationService.showTestResultAvailableNotification(testResult) } just Runs
        coEvery {
            notificationHelper.cancelCurrentNotification(
                NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
            )
        } just Runs

        val result = createWorker().doWork()

        coVerify {
            coronaTestRepository.refresh(type = CoronaTest.Type.PCR)
            testResultAvailableNotificationService.showTestResultAvailableNotification(testResult)
            notificationHelper.cancelCurrentNotification(
                NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
            )
            coronaTestRepository.updateResultNotification(any(), sent = true)
        }

        result shouldBe ListenableWorker.Result.success()
    }

    @Test
    fun testSendNoNotificationWhenPending() = runBlockingTest {
        val testResult = CoronaTestResult.PCR_OR_RAT_PENDING
        coronaTestFlow.value = setOf(newCoronaTest(result = testResult))
        coEvery { testResultAvailableNotificationService.showTestResultAvailableNotification(testResult) } just Runs
        coEvery {
            notificationHelper.cancelCurrentNotification(
                NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
            )
        } just Runs

        val result = createWorker().doWork()

        coVerify { coronaTestRepository.refresh(type = CoronaTest.Type.PCR) }
        coVerify(exactly = 0) {
            testResultAvailableNotificationService.showTestResultAvailableNotification(
                testResult
            )
            notificationHelper.cancelCurrentNotification(
                NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
            )
            testResultScheduler.setPcrPeriodicTestPollingEnabled(enabled = false)
        }

        result shouldBe ListenableWorker.Result.success()
    }

    @Test
    fun testRetryWhenExceptionIsThrown() = runBlockingTest {
        coronaTestFlow.value = setOf(newCoronaTest())
        coEvery { coronaTestRepository.refresh(any()) } throws Exception()

        val result = createWorker().doWork()

        coVerify(exactly = 1) { coronaTestRepository.refresh(type = CoronaTest.Type.PCR) }
        coVerify(exactly = 0) { testResultScheduler.setPcrPeriodicTestPollingEnabled(any()) }
        result shouldBe ListenableWorker.Result.retry()
    }
}
