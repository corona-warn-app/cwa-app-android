package de.rki.coronawarnapp.http.playbook

import de.rki.coronawarnapp.playbook.DefaultPlaybook
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.submission.server.SubmissionServer
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.verification.server.VerificationKeyType
import de.rki.coronawarnapp.verification.server.VerificationServer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
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
        coEvery { verificationServer.retrieveTestResults(any()) } returns 0
        coEvery { verificationServer.retrieveTanFake() } returns mockk()
        coEvery { verificationServer.retrieveTan(any()) } returns "tan"

        coEvery { submissionServer.submitKeysToServer(any()) } returns mockk()
        coEvery { submissionServer.submitKeysToServerFake() } returns mockk()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
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
            verificationServer.retrieveTestResults(any())
            submissionServer.submitKeysToServerFake()
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
            submissionServer.submitKeysToServerFake()
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
                visistedCountries = listOf("DE")
            )
        )

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveTan(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitKeysToServer(any())
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
                    visistedCountries = listOf("DE")
                )
            )
        }

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveTan(any())
            verificationServer.retrieveTanFake()
            // Only called when null TAN is returned? But when does that happen?
            submissionServer.submitKeysToServerFake()
        }
    }

    @Test
    fun `test result retrieval matches pattern`(): Unit = runBlocking {
        coEvery { verificationServer.retrieveTestResults(any()) } returns 0

        createPlaybook().testResult("token")

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveTestResults(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitKeysToServerFake()
        }
    }

    @Test
    fun `dummy request pattern matches`(): Unit = runBlocking {
        createPlaybook().dummy()

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveTanFake()
            verificationServer.retrieveTanFake()
            submissionServer.submitKeysToServerFake()
        }
    }

    @Test
    fun `failures during dummy requests should be ignored`(): Unit = runBlocking {
        val expectedToken = "token"
        coEvery { verificationServer.retrieveRegistrationToken(any(), any()) } returns expectedToken
        val expectedResult = TestResult.PENDING
        coEvery { verificationServer.retrieveTestResults(expectedToken) } returns expectedResult.value
        coEvery { submissionServer.submitKeysToServerFake() } throws TestException()

        val (registrationToken, testResult) = createPlaybook()
            .initialRegistration("key", VerificationKeyType.GUID)

        registrationToken shouldBe expectedToken
        testResult shouldBe expectedResult
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
            submissionServer.submitKeysToServerFake()
        }
    }

    @Test
    fun `registration pattern matches despite test result failure`(): Unit = runBlocking {
        coEvery { verificationServer.retrieveTestResults(any()) } throws TestException()

        shouldThrow<TestException> {
            createPlaybook().initialRegistration("9A3B578UMG", VerificationKeyType.TELETAN)
        }

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveRegistrationToken(any(), any())
            verificationServer.retrieveTestResults(any())
            submissionServer.submitKeysToServerFake()
        }
    }

    @Test
    fun `test result pattern matches despite failure`(): Unit = runBlocking {
        coEvery { verificationServer.retrieveTestResults(any()) } throws TestException()

        shouldThrow<TestException> {
            createPlaybook().testResult("token")
        }

        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveTestResults(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitKeysToServerFake()
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
                    visistedCountries = listOf("DE")
                )
            )
        }
        coVerifySequence {
            // ensure request order is 2x verification and 1x submission
            verificationServer.retrieveTan(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitKeysToServerFake()
        }
    }
}
