package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Operation
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.notification.TestResultAvailableNotificationService
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.util.TimeAndDateExtensions.daysToMilliseconds
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.util.encryptionmigration.EncryptedPreferencesFactory
import de.rki.coronawarnapp.util.encryptionmigration.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler.stop
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class DiagnosisTestResultRetrievalPeriodicWorkerTest : BaseTest() {
    @MockK lateinit var context: Context
    @MockK lateinit var request: WorkRequest
    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var submissionService: SubmissionService
    @MockK lateinit var testResultAvailableNotificationService: TestResultAvailableNotificationService
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var appComponent: ApplicationComponent
    @MockK lateinit var encryptedPreferencesFactory: EncryptedPreferencesFactory
    @MockK lateinit var encryptionErrorResetTool: EncryptionErrorResetTool
    @MockK lateinit var operation: Operation
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var tracingSettings: TracingSettings
    @RelaxedMockK lateinit var workerParams: WorkerParameters
    private val currentInstant = Instant.ofEpochSecond(1611764225)
    private val registrationToken = "test token"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { submissionSettings.hasViewedTestResult.value } returns false
        every { timeStamper.nowUTC } returns currentInstant
        every { tracingSettings.initialPollingForTestResultTimeStamp } returns currentInstant.millis
        every { tracingSettings.isTestResultAvailableNotificationSent } returns false
        every { tracingSettings.initialPollingForTestResultTimeStamp = capture(slot()) } answers {}
        every { tracingSettings.isTestResultAvailableNotificationSent = capture(slot()) } answers {}

        mockkObject(AppInjector)
        every { AppInjector.component } returns appComponent
        every { appComponent.encryptedPreferencesFactory } returns encryptedPreferencesFactory
        every { appComponent.errorResetTool } returns encryptionErrorResetTool

        every { submissionSettings.registrationToken } returns mockFlowPreference(registrationToken)

        mockkObject(BackgroundWorkScheduler)
        every { BackgroundWorkScheduler.WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.stop() } returns operation
    }

    @Test
    fun testStopWorkerWhenResultHasBeenViewed() {
        runBlockingTest {
            every { submissionSettings.hasViewedTestResult.value } returns true
            val worker = createWorker()
            val result = worker.doWork()
            coVerify(exactly = 0) { submissionService.asyncRequestTestResult(any()) }
            verify(exactly = 1) { BackgroundWorkScheduler.WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.stop() }
            result shouldBe ListenableWorker.Result.success()
        }
    }

    @Test
    fun testStopWorkerWhenNotificationSent() {
        runBlockingTest {
            every { tracingSettings.isTestResultAvailableNotificationSent } returns true
            val worker = createWorker()
            val result = worker.doWork()
            coVerify(exactly = 0) { submissionService.asyncRequestTestResult(any()) }
            verify(exactly = 1) { BackgroundWorkScheduler.WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.stop() }
            result shouldBe ListenableWorker.Result.success()
        }
    }

    @Test
    fun testStopWorkerWhenMaxDaysExceeded() {
        runBlockingTest {
            val past =
                currentInstant - (BackgroundConstants.POLLING_VALIDITY_MAX_DAYS.toLong() + 1).daysToMilliseconds()
            every { tracingSettings.initialPollingForTestResultTimeStamp } returns past.millis
            val worker = createWorker()
            val result = worker.doWork()
            coVerify(exactly = 0) { submissionService.asyncRequestTestResult(any()) }
            verify(exactly = 1) { BackgroundWorkScheduler.WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.stop() }
            result shouldBe ListenableWorker.Result.success()
        }
    }

    @Test
    fun testSendNotificationWhenPositive() {
        val isTestResultAvailableNotificationSent = slot<Boolean>()
        every {
            tracingSettings.isTestResultAvailableNotificationSent = capture(isTestResultAvailableNotificationSent)
        } answers {}

        runBlockingTest {
            val testResult = TestResult.POSITIVE
            coEvery { submissionService.asyncRequestTestResult(registrationToken) } returns testResult
            coEvery { testResultAvailableNotificationService.showTestResultAvailableNotification(testResult) } just Runs
            coEvery {
                notificationHelper.cancelCurrentNotification(
                    NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
                )
            } just Runs
            val worker = createWorker()
            val result = worker.doWork()
            coVerify { submissionService.asyncRequestTestResult(registrationToken) }
            coVerify { testResultAvailableNotificationService.showTestResultAvailableNotification(testResult) }
            coVerify {
                notificationHelper.cancelCurrentNotification(
                    NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
                )
            }
            result shouldBe ListenableWorker.Result.success()
            isTestResultAvailableNotificationSent.captured shouldBe true
        }
    }

    @Test
    fun testSendNotificationWhenNegative() {
        val isTestResultAvailableNotificationSent = slot<Boolean>()
        every {
            tracingSettings.isTestResultAvailableNotificationSent = capture(isTestResultAvailableNotificationSent)
        } answers {}

        runBlockingTest {
            val testResult = TestResult.NEGATIVE
            coEvery { submissionService.asyncRequestTestResult(registrationToken) } returns testResult
            coEvery { testResultAvailableNotificationService.showTestResultAvailableNotification(testResult) } just Runs
            coEvery {
                notificationHelper.cancelCurrentNotification(
                    NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
                )
            } just Runs
            val worker = createWorker()
            val result = worker.doWork()
            coVerify { submissionService.asyncRequestTestResult(registrationToken) }
            coVerify { testResultAvailableNotificationService.showTestResultAvailableNotification(testResult) }
            coVerify {
                notificationHelper.cancelCurrentNotification(
                    NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
                )
            }
            result shouldBe ListenableWorker.Result.success()
            isTestResultAvailableNotificationSent.captured shouldBe true
        }
    }

    @Test
    fun testSendNotificationWhenInvalid() {
        val isTestResultAvailableNotificationSent = slot<Boolean>()
        every {
            tracingSettings.isTestResultAvailableNotificationSent = capture(isTestResultAvailableNotificationSent)
        } answers {}

        runBlockingTest {
            val testResult = TestResult.INVALID
            coEvery { submissionService.asyncRequestTestResult(registrationToken) } returns testResult
            coEvery { testResultAvailableNotificationService.showTestResultAvailableNotification(testResult) } just Runs
            coEvery {
                notificationHelper.cancelCurrentNotification(
                    NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
                )
            } just Runs
            val worker = createWorker()
            val result = worker.doWork()
            coVerify { submissionService.asyncRequestTestResult(registrationToken) }
            coVerify { testResultAvailableNotificationService.showTestResultAvailableNotification(testResult) }
            coVerify {
                notificationHelper.cancelCurrentNotification(
                    NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
                )
            }
            result shouldBe ListenableWorker.Result.success()
            isTestResultAvailableNotificationSent.captured shouldBe true
        }
    }

    @Test
    fun testSendNoNotificationWhenPending() {
        runBlockingTest {
            val testResult = TestResult.PENDING
            coEvery { submissionService.asyncRequestTestResult(registrationToken) } returns testResult
            coEvery { testResultAvailableNotificationService.showTestResultAvailableNotification(testResult) } just Runs
            coEvery {
                notificationHelper.cancelCurrentNotification(
                    NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
                )
            } just Runs
            val worker = createWorker()
            val result = worker.doWork()
            coVerify { submissionService.asyncRequestTestResult(registrationToken) }
            coVerify(exactly = 0) {
                testResultAvailableNotificationService.showTestResultAvailableNotification(
                    testResult
                )
            }
            coVerify(exactly = 0) {
                notificationHelper.cancelCurrentNotification(
                    NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
                )
            }
            coVerify(exactly = 0) { BackgroundWorkScheduler.WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.stop() }
            result shouldBe ListenableWorker.Result.success()
        }
    }

    @Test
    fun testRetryWhenExceptionIsThrown() {
        runBlockingTest {
            coEvery { submissionService.asyncRequestTestResult(registrationToken) } throws Exception()
            val worker = createWorker()
            val result = worker.doWork()
            coVerify(exactly = 1) { submissionService.asyncRequestTestResult(any()) }
            coVerify(exactly = 0) { BackgroundWorkScheduler.WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.stop() }
            result shouldBe ListenableWorker.Result.retry()
        }
    }

    private fun createWorker() = DiagnosisTestResultRetrievalPeriodicWorker(
        context,
        workerParams,
        testResultAvailableNotificationService,
        notificationHelper,
        submissionSettings,
        submissionService,
        timeStamper,
        tracingSettings
    )
}
