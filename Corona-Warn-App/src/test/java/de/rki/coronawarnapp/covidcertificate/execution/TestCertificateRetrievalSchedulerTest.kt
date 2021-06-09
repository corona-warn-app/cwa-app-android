package de.rki.coronawarnapp.covidcertificate.execution

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.test.execution.TestCertificateRetrievalScheduler
import de.rki.coronawarnapp.util.device.ForegroundState
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import testhelpers.gms.MockListenableFuture

class TestCertificateRetrievalSchedulerTest : BaseTest() {

    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var certificateRepo: TestCertificateRepository
    @MockK lateinit var testRepo: CoronaTestRepository
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var workInfo: WorkInfo

    private val mockTest = mockk<CoronaTest>().apply {
        every { identifier } returns "identifier1"
        every { isDccConsentGiven } returns true
        every { isDccDataSetCreated } returns false
        every { isDccSupportedByPoc } returns true
        every { isNegative } returns true
    }

    private val mockCertificate = mockk<TestCertificateWrapper>().apply {
        every { identifier } returns "UUID"
        every { isCertificateRetrievalPending } returns true
        every { isUpdatingData } returns false
    }

    private val testsFlow = MutableStateFlow(setOf(mockTest))
    private val certificatesFlow = MutableStateFlow(setOf(mockCertificate))
    private val foregroundFlow = MutableStateFlow(false)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        workManager.apply {
            every { enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } returns mockk()
            every { getWorkInfosForUniqueWork(any()) } returns MockListenableFuture.forResult(listOf(workInfo))
        }

        every { workInfo.state } returns WorkInfo.State.SUCCEEDED

        testRepo.apply {
            every { testRepo.coronaTests } returns testsFlow
            coEvery { markDccAsCreated(any(), any()) } just Runs
        }
        certificateRepo.apply {
            every { certificates } returns certificatesFlow
            coEvery { requestCertificate(any()) } returns mockk()
        }

        every { foregroundState.isInForeground } returns foregroundFlow
    }

    private fun createInstance(scope: CoroutineScope) = TestCertificateRetrievalScheduler(
        appScope = scope,
        workManager = workManager,
        certificateRepo = certificateRepo,
        foregroundState = foregroundState,
        testRepo = testRepo,
    )

    @Test
    fun `new negative corona tests create a dcc if supported and consented`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).setup()
        coVerify { certificateRepo.requestCertificate(mockTest) }
    }

    @Test
    fun `certificates only for negative results`() = runBlockingTest2(ignoreActive = true) {
        every { mockTest.isNegative } returns false
        createInstance(scope = this).setup()
        advanceUntilIdle()
        coVerify(exactly = 0) { certificateRepo.requestCertificate(any()) }
    }

    @Test
    fun `no duplicate certificates for flaky test results`() = runBlockingTest2(ignoreActive = true) {
        every { mockTest.isDccDataSetCreated } returns true
        createInstance(scope = this).setup()
        advanceUntilIdle()
        coVerify(exactly = 0) { certificateRepo.requestCertificate(any()) }
    }

    @Test
    fun `refresh on foreground`() = runBlockingTest2(ignoreActive = true) {
        testsFlow.value = emptySet()

        createInstance(scope = this).setup()
        advanceUntilIdle()
        coVerify(exactly = 1) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }

        foregroundFlow.value = true
        advanceUntilIdle()
        coVerify(exactly = 2) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `refresh on new certificate entry`() = runBlockingTest2(ignoreActive = true) {
        testsFlow.value = emptySet()
        createInstance(scope = this).setup()

        advanceUntilIdle()
        coVerify(exactly = 1) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }

        val mockCertificate2 = mockk<TestCertificateWrapper>().apply {
            every { identifier } returns "UUID2"
            every { isCertificateRetrievalPending } returns true
            every { isUpdatingData } returns false
        }

        certificatesFlow.value = setOf(mockCertificate, mockCertificate2)
        advanceUntilIdle()
        coVerify(exactly = 2) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }
}
