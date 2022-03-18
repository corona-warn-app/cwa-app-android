package de.rki.coronawarnapp.covidcertificate.test.core.execution

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import de.rki.coronawarnapp.familytest.core.model.FamilyTest
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
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
    @MockK lateinit var testCertificateRepository: TestCertificateRepository
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var familyTestRepository: FamilyTestRepository
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
        every { containerId } returns TestCertificateContainerId("UUID")
        every { isCertificateRetrievalPending } returns true
        every { isUpdatingData } returns false
    }

    private val mockFamilyTest = mockk<FamilyTest>().apply {
        every { identifier } returns "identifier1-family"
        every { isDccConsentGiven } returns true
        every { isDccDataSetCreated } returns false
        every { isDccSupportedByPoc } returns true
        every { isNegative } returns true
    }

    private val mockFamilyCertificate = mockk<TestCertificateWrapper>().apply {
        every { containerId } returns TestCertificateContainerId("UUID-family")
        every { isCertificateRetrievalPending } returns true
        every { isUpdatingData } returns false
    }

    private val testsFlow = MutableStateFlow(setOf(mockTest))
    private val familyTestsFlow = MutableStateFlow(setOf(mockFamilyTest))
    private val certificatesFlow = MutableStateFlow(setOf(mockCertificate, mockFamilyCertificate))
    private val foregroundFlow = MutableStateFlow(false)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        workManager.apply {
            every { enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } returns mockk()
            every { getWorkInfosForUniqueWork(any()) } returns MockListenableFuture.forResult(listOf(workInfo))
        }

        every { workInfo.state } returns WorkInfo.State.SUCCEEDED

        coronaTestRepository.apply {
            every { coronaTestRepository.coronaTests } returns testsFlow
            coEvery { markDccAsCreated(any(), any()) } just Runs
        }

        familyTestRepository.apply {
            every { familyTests } returns familyTestsFlow
            coEvery { markDccAsCreated(any(), any()) } just Runs
        }

        testCertificateRepository.apply {
            every { certificates } returns certificatesFlow
            coEvery { requestCertificate(any()) } returns mockk()
        }

        every { foregroundState.isInForeground } returns foregroundFlow
    }

    private fun createInstance(scope: CoroutineScope) = TestCertificateRetrievalScheduler(
        appScope = scope,
        workManager = workManager,
        testCertificateRepository = testCertificateRepository,
        foregroundState = foregroundState,
        coronaTestRepository = coronaTestRepository,
        familyTestRepository = familyTestRepository,
    )

    @Test
    fun `new negative corona tests create a dcc if supported and consented`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).setup()
        coVerify {
            testCertificateRepository.requestCertificate(mockTest)
            coronaTestRepository.markDccAsCreated("identifier1", true)

            testCertificateRepository.requestCertificate(mockFamilyTest)
            familyTestRepository.markDccAsCreated("identifier1-family", true)
        }
    }

    @Test
    fun `certificates only for negative results`() = runBlockingTest2(ignoreActive = true) {
        every { mockTest.isNegative } returns false
        every { mockFamilyTest.isNegative } returns false
        createInstance(scope = this).setup()
        advanceUntilIdle()
        coVerify(exactly = 0) { testCertificateRepository.requestCertificate(any()) }
    }

    @Test
    fun `no duplicate certificates for flaky test results`() = runBlockingTest2(ignoreActive = true) {
        every { mockTest.isDccDataSetCreated } returns true
        every { mockFamilyTest.isDccDataSetCreated } returns true
        createInstance(scope = this).setup()
        advanceUntilIdle()
        coVerify(exactly = 0) { testCertificateRepository.requestCertificate(any()) }
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
            every { containerId } returns TestCertificateContainerId("UUID2")
            every { isCertificateRetrievalPending } returns true
            every { isUpdatingData } returns false
        }

        certificatesFlow.value = setOf(mockCertificate, mockFamilyCertificate, mockCertificate2)
        advanceUntilIdle()
        coVerify(exactly = 2) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }
}
