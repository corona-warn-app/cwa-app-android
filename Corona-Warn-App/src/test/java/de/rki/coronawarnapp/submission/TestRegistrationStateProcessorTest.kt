package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

class TestRegistrationStateProcessorTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var familyTestRepository: FamilyTestRepository
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var registeredTestRA: BaseCoronaTest
    @MockK lateinit var registeredTestPCR: BaseCoronaTest

    private val raRequest: TestRegistrationRequest = CoronaTestQRCode.RapidAntigen(
        hash = "ra-hash",
        createdAt = Instant.EPOCH,
        rawQrCode = "rawQrCode"
    )
    private val pcrRequest: TestRegistrationRequest = CoronaTestQRCode.PCR(
        qrCodeGUID = "pcr-guid",
        rawQrCode = "rawQrCode"
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        submissionRepository.apply {
            coEvery { registerTest(raRequest) } returns registeredTestRA
            coEvery { registerTest(pcrRequest) } returns registeredTestPCR

            coEvery { tryReplaceTest(raRequest) } returns registeredTestRA
            coEvery { tryReplaceTest(pcrRequest) } returns registeredTestPCR

            coEvery { giveConsentToSubmission(any()) } just Runs
        }

        registeredTestRA.apply {
            every { type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        }
        registeredTestPCR.apply {
            every { type } returns BaseCoronaTest.Type.PCR
        }

        coEvery { analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any()) } just Runs
    }

    private fun createInstance() = TestRegistrationStateProcessor(
        submissionRepository = submissionRepository,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        familyTestRepository = familyTestRepository
    )

    @Test
    fun `register new RA test - with consent`() = runTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startTestRegistration(
            request = raRequest,
            isSubmissionConsentGiven = true,
            allowTestReplacement = false
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTestRA
        )

        coVerify {
            submissionRepository.registerTest(raRequest)
            submissionRepository.giveConsentToSubmission(BaseCoronaTest.Type.RAPID_ANTIGEN)
            analyticsKeySubmissionCollector.reportAdvancedConsentGiven(BaseCoronaTest.Type.RAPID_ANTIGEN)
        }
    }

    @Test
    fun `register new RA test - without consent`() = runTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startTestRegistration(
            request = raRequest,
            isSubmissionConsentGiven = false,
            allowTestReplacement = false
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTestRA
        )

        coVerify {
            submissionRepository.registerTest(raRequest)
        }
        coVerify(exactly = 0) {
            submissionRepository.giveConsentToSubmission(any())
            analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
        }
    }

    @Test
    fun `replace RA test - with consent`() = runTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startTestRegistration(
            request = raRequest,
            isSubmissionConsentGiven = true,
            allowTestReplacement = true
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTestRA
        )

        coVerify {
            submissionRepository.tryReplaceTest(raRequest)
            submissionRepository.giveConsentToSubmission(BaseCoronaTest.Type.RAPID_ANTIGEN)
            analyticsKeySubmissionCollector.reportAdvancedConsentGiven(BaseCoronaTest.Type.RAPID_ANTIGEN)
        }
    }

    @Test
    fun `replace RA new test - without consent`() = runTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startTestRegistration(
            request = raRequest,
            isSubmissionConsentGiven = false,
            allowTestReplacement = true
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTestRA
        )

        coVerify {
            submissionRepository.tryReplaceTest(raRequest)
        }
        coVerify(exactly = 0) {
            submissionRepository.giveConsentToSubmission(any())
            analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
        }
    }

    @Test
    fun `register new PCR test - with consent`() = runTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startTestRegistration(
            request = pcrRequest,
            isSubmissionConsentGiven = true,
            allowTestReplacement = false
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTestPCR
        )

        coVerify {
            submissionRepository.registerTest(pcrRequest)
            submissionRepository.giveConsentToSubmission(BaseCoronaTest.Type.PCR)
            analyticsKeySubmissionCollector.reportAdvancedConsentGiven(BaseCoronaTest.Type.PCR)
        }
    }

    @Test
    fun `register new PCR test - without consent`() = runTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startTestRegistration(
            request = pcrRequest,
            isSubmissionConsentGiven = false,
            allowTestReplacement = false
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTestPCR
        )

        coVerify {
            submissionRepository.registerTest(pcrRequest)
        }
        coVerify(exactly = 0) {
            submissionRepository.giveConsentToSubmission(any())
            analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
        }
    }

    @Test
    fun `replace PCR test - with consent`() = runTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startTestRegistration(
            request = pcrRequest,
            isSubmissionConsentGiven = true,
            allowTestReplacement = true
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTestPCR
        )

        coVerify {
            submissionRepository.tryReplaceTest(pcrRequest)
            submissionRepository.giveConsentToSubmission(BaseCoronaTest.Type.PCR)
            analyticsKeySubmissionCollector.reportAdvancedConsentGiven(BaseCoronaTest.Type.PCR)
        }
    }

    @Test
    fun `replace PCR new test - without consent`() = runTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startTestRegistration(
            request = pcrRequest,
            isSubmissionConsentGiven = false,
            allowTestReplacement = true
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTestPCR
        )

        coVerify {
            submissionRepository.tryReplaceTest(pcrRequest)
        }
        coVerify(exactly = 0) {
            submissionRepository.giveConsentToSubmission(any())
            analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
        }
    }

    @Test
    fun `errors are mapped to state`() = runTest {
        val instance = createInstance()

        val expectedException = BadRequestException("")
        coEvery { submissionRepository.registerTest(any()) } throws expectedException

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startTestRegistration(
            request = raRequest,
            isSubmissionConsentGiven = true,
            allowTestReplacement = false
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Error(
            exception = expectedException
        )

        coVerify {
            submissionRepository.registerTest(raRequest)
        }
        coVerify(exactly = 0) {
            submissionRepository.giveConsentToSubmission(any())
        }
    }
}
