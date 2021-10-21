package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.exception.http.BadRequestException
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TestRegistrationStateProcessorTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var registeredTestRA: CoronaTest
    @MockK lateinit var registeredTestPCR: CoronaTest

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
            every { type } returns CoronaTest.Type.RAPID_ANTIGEN
        }
        registeredTestPCR.apply {
            every { type } returns CoronaTest.Type.PCR
        }

        every { analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any()) } just Runs
    }

    private fun createInstance() = TestRegistrationStateProcessor(
        submissionRepository = submissionRepository,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
    )

    @Test
    fun `register new RA test - with consent`() = runBlockingTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = raRequest,
            isSubmissionConsentGiven = true,
            allowReplacement = false
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTestRA
        )

        coVerify {
            submissionRepository.registerTest(raRequest)
            submissionRepository.giveConsentToSubmission(CoronaTest.Type.RAPID_ANTIGEN)
            analyticsKeySubmissionCollector.reportAdvancedConsentGiven(CoronaTest.Type.RAPID_ANTIGEN)
        }
    }

    @Test
    fun `register new RA test - without consent`() = runBlockingTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = raRequest,
            isSubmissionConsentGiven = false,
            allowReplacement = false
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
    fun `replace RA test - with consent`() = runBlockingTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = raRequest,
            isSubmissionConsentGiven = true,
            allowReplacement = true
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTestRA
        )

        coVerify {
            submissionRepository.tryReplaceTest(raRequest)
            submissionRepository.giveConsentToSubmission(CoronaTest.Type.RAPID_ANTIGEN)
            analyticsKeySubmissionCollector.reportAdvancedConsentGiven(CoronaTest.Type.RAPID_ANTIGEN)
        }
    }

    @Test
    fun `replace RA new test - without consent`() = runBlockingTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = raRequest,
            isSubmissionConsentGiven = false,
            allowReplacement = true
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
    fun `register new PCR test - with consent`() = runBlockingTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = pcrRequest,
            isSubmissionConsentGiven = true,
            allowReplacement = false
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTestPCR
        )

        coVerify {
            submissionRepository.registerTest(pcrRequest)
            submissionRepository.giveConsentToSubmission(CoronaTest.Type.PCR)
            analyticsKeySubmissionCollector.reportAdvancedConsentGiven(CoronaTest.Type.PCR)
        }
    }

    @Test
    fun `register new PCR test - without consent`() = runBlockingTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = pcrRequest,
            isSubmissionConsentGiven = false,
            allowReplacement = false
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
    fun `replace PCR test - with consent`() = runBlockingTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = pcrRequest,
            isSubmissionConsentGiven = true,
            allowReplacement = true
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTestPCR
        )

        coVerify {
            submissionRepository.tryReplaceTest(pcrRequest)
            submissionRepository.giveConsentToSubmission(CoronaTest.Type.PCR)
            analyticsKeySubmissionCollector.reportAdvancedConsentGiven(CoronaTest.Type.PCR)
        }
    }

    @Test
    fun `replace PCR new test - without consent`() = runBlockingTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = pcrRequest,
            isSubmissionConsentGiven = false,
            allowReplacement = true
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
    fun `errors are mapped to state`() = runBlockingTest {
        val instance = createInstance()

        val expectedException = BadRequestException("")
        coEvery { submissionRepository.registerTest(any()) } throws expectedException

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = raRequest,
            isSubmissionConsentGiven = true,
            allowReplacement = false
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
