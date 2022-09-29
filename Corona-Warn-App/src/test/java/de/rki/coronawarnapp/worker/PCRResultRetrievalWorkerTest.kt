package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.execution.PCRResultRetrievalWorker
import de.rki.coronawarnapp.coronatest.type.pcr.execution.PCRResultScheduler
import de.rki.coronawarnapp.notification.GeneralNotifications
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTest
import java.time.Instant

class PCRResultRetrievalWorkerTest : BaseTest() {
    @MockK lateinit var context: Context
    @MockK lateinit var request: WorkRequest
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var appComponent: ApplicationComponent
    @MockK lateinit var encryptedPreferencesFactory: EncryptedPreferencesFactory
    @MockK lateinit var encryptionErrorResetTool: EncryptionErrorResetTool
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var testResultScheduler: PCRResultScheduler

    @RelaxedMockK lateinit var workerParams: WorkerParameters
    private val currentInstant = Instant.ofEpochSecond(1611764225)
    private val testToken = "test token"

    private val coronaTestFlow = MutableStateFlow(emptySet<PersonalCoronaTest>())

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns currentInstant

        mockkObject(AppInjector)
        every { AppInjector.component } returns appComponent
        every { appComponent.encryptedPreferencesFactory } returns encryptedPreferencesFactory
        every { appComponent.errorResetTool } returns encryptionErrorResetTool

        coEvery { testResultScheduler.setPcrPeriodicTestPollingEnabled(enabled = any()) } just Runs

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
    ): PersonalCoronaTest {
        return mockk<PCRCoronaTest>().apply {
            every { identifier } returns ""
            every { type } returns BaseCoronaTest.Type.PCR
            every { registeredAt } returns registered
            every { isViewed } returns viewed
            every { testResult } returns result
            every { registrationToken } returns testToken
            every { isResultAvailableNotificationSent } returns isNotificationSent
        }
    }

    private fun createWorker() = PCRResultRetrievalWorker(
        context = context,
        workerParams = workerParams,
        coronaTestRepository = coronaTestRepository,
    )

    @Test
    fun testSuccess() = runTest {
        coronaTestFlow.value = setOf(newCoronaTest())

        val result = createWorker().doWork()

        coVerify(exactly = 1) { coronaTestRepository.refresh(type = BaseCoronaTest.Type.PCR) }
        result shouldBe ListenableWorker.Result.success()
    }

    @Test
    fun testRetryWhenExceptionIsThrown() = runTest {
        coronaTestFlow.value = setOf(newCoronaTest())
        coEvery { coronaTestRepository.refresh(any()) } throws Exception()

        val result = createWorker().doWork()

        coVerify(exactly = 1) { coronaTestRepository.refresh(type = BaseCoronaTest.Type.PCR) }
        result shouldBe ListenableWorker.Result.retry()
    }
}
