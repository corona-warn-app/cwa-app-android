package de.rki.coronawarnapp.http.playbook

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.CoronaTestResultResponse
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.exception.TanPairingException
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.playbook.DefaultPlaybook
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload.SubmissionType
import de.rki.coronawarnapp.submission.server.SubmissionServer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.exceptions.TestException

class DefaultPlaybookTest : BaseTest() {

    @MockK lateinit var submissionServer: SubmissionServer
    @MockK lateinit var verificationServer: VerificationServer

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { verificationServer.retrieveRegistrationToken(any(), any()) } returns "token"
        coEvery { verificationServer.pollTestResult(any()) } returns CoronaTestResultResponse(
            coronaTestResult = CoronaTestResult.PCR_OR_RAT_PENDING,
            sampleCollectedAt = null
        )
        coEvery { verificationServer.retrieveTanFake() } returns mockk()
        coEvery { verificationServer.retrieveTan(any()) } returns "tan"

        coEvery { submissionServer.submitPayload(any()) } returns mockk()
        coEvery { submissionServer.submitFakePayload() } returns mockk()
    }

    private fun createPlaybook() = DefaultPlaybook(
        verificationServer = verificationServer,
        submissionServer = submissionServer
    )

    @Test
    fun `initial registration pattern matches`(): Unit = runBlocking {
        coEvery { verificationServer.retrieveRegistrationToken(any(), any()) } returns "response"

        createPlaybook().initialRegistration("9A3B578UMG", VerificationKeyType.TELETAN)

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveRegistrationToken(any(), any())
            verificationServer.pollTestResult(any())
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun ` registration pattern matches despite token failure`(): Unit = runBlocking {
        coEvery {
            verificationServer.retrieveRegistrationToken(any(), any())
        } throws TestException()

        shouldThrow<TestException> {
            createPlaybook().initialRegistration("9A3B578UMG", VerificationKeyType.TELETAN)
        }

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveRegistrationToken(any(), any())
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `submission matches request pattern`(): Unit = runBlocking {
        coEvery { verificationServer.retrieveTan(any()) } returns "tan"

        createPlaybook().submit(
            Playbook.SubmissionData(
                registrationToken = "token",
                temporaryExposureKeys = listOf(),
                consentToFederation = true,
                visitedCountries = listOf("DE"),
                checkIns = emptyList(),
                submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST
            )
        )

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveTan(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitPayload(any())
        }
    }

    @Test
    fun `tan retrieval throws human readable exception`(): Unit = runBlocking {
        coEvery { verificationServer.retrieveTan(any()) } throws BadRequestException(null)
        try {
            createPlaybook().submit(
                Playbook.SubmissionData(
                    registrationToken = "token",
                    temporaryExposureKeys = listOf(),
                    consentToFederation = true,
                    visitedCountries = listOf("DE"),
                    checkIns = emptyList(),
                    submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST
                )
            )
        } catch (e: Exception) {
            e.shouldBeInstanceOf<TanPairingException>()
            e.cause.shouldBeInstanceOf<BadRequestException>()
            e.message shouldBe "Tan has been retrieved before for this registration token"
        }
    }

    @Test
    fun `keys submission throws human readable exception`(): Unit = runBlocking {
        coEvery { submissionServer.submitPayload(any()) } throws BadRequestException(null)
        try {
            createPlaybook().submit(
                Playbook.SubmissionData(
                    registrationToken = "token",
                    temporaryExposureKeys = listOf(),
                    consentToFederation = true,
                    visitedCountries = listOf("DE"),
                    checkIns = emptyList(),
                    submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST
                )
            )
        } catch (e: Exception) {
            e.shouldBeInstanceOf<TanPairingException>()
            e.cause.shouldBeInstanceOf<BadRequestException>()
            e.message shouldBe "Invalid payload or missing header"
        }
    }

    @Test
    fun `submission matches request pattern despite missing authcode`(): Unit = runBlocking {
        coEvery { verificationServer.retrieveTan(any()) } throws TestException()

        shouldThrow<TestException> {
            createPlaybook().submit(
                Playbook.SubmissionData(
                    registrationToken = "token",
                    temporaryExposureKeys = listOf(),
                    consentToFederation = true,
                    visitedCountries = listOf("DE"),
                    checkIns = emptyList(),
                    submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST
                )
            )
        }

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveTan(any())
            verificationServer.retrieveTanFake()
            // Only called when null TAN is returned? But when does that happen?
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `test result retrieval matches pattern`(): Unit = runBlocking {
        createPlaybook().testResult("token")

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.pollTestResult(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `dummy request pattern matches`(): Unit = runBlocking {
        createPlaybook().dummy()

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveTanFake()
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `failures during dummy requests should be ignored`(): Unit = runBlocking {
        val expectedToken = "token"
        coEvery { verificationServer.retrieveRegistrationToken(any(), any()) } returns expectedToken
        val expectedResult = CoronaTestResult.PCR_OR_RAT_PENDING
        coEvery { verificationServer.pollTestResult(expectedToken) } returns CoronaTestResultResponse(
            coronaTestResult = expectedResult,
            sampleCollectedAt = null
        )
        coEvery { submissionServer.submitFakePayload() } throws TestException()

        val (registrationToken, testResult) = createPlaybook()
            .initialRegistration("key", VerificationKeyType.GUID)

        registrationToken shouldBe expectedToken
        testResult shouldBe CoronaTestResultResponse(
            coronaTestResult = expectedResult,
            sampleCollectedAt = null
        )
    }

    @Test
    fun `registration pattern matches despire token failure`(): Unit = runBlocking {
        coEvery {
            verificationServer.retrieveRegistrationToken(any(), any())
        } throws TestException()

        shouldThrow<TestException> {
            createPlaybook().initialRegistration("9A3B578UMG", VerificationKeyType.TELETAN)
        }
        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveRegistrationToken(any(), any())
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `registration pattern matches despite test result failure`(): Unit = runBlocking {
        coEvery { verificationServer.pollTestResult(any()) } throws TestException()

        shouldThrow<TestException> {
            createPlaybook().initialRegistration("9A3B578UMG", VerificationKeyType.TELETAN)
        }

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveRegistrationToken(any(), any())
            verificationServer.pollTestResult(any())
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `test result pattern matches despite failure`(): Unit = runBlocking {
        coEvery { verificationServer.pollTestResult(any()) } throws TestException()

        shouldThrow<TestException> {
            createPlaybook().testResult("token")
        }

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.pollTestResult(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `submission pattern matches despite tan failure`(): Unit = runBlocking {
        coEvery { verificationServer.retrieveTan(any()) } throws TestException()

        shouldThrow<TestException> {
            createPlaybook().submit(
                Playbook.SubmissionData(
                    registrationToken = "token",
                    temporaryExposureKeys = listOf(),
                    consentToFederation = true,
                    visitedCountries = listOf("DE"),
                    checkIns = emptyList(),
                    submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST
                )
            )
        }
        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveTan(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }
}
