package de.rki.coronawarnapp.playbook

import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationApiV1
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.presencetracing.organizer.submission.OrganizerSubmissionException
import de.rki.coronawarnapp.presencetracing.organizer.submission.server.OrganizerSubmissionServer
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.submission.server.SubmissionServer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider

internal class OrganizerPlaybookTest : BaseTest() {

    @MockK lateinit var verificationServer: VerificationServer
    @MockK lateinit var organizerSubmissionServer: OrganizerSubmissionServer
    @MockK lateinit var submissionServer: SubmissionServer
    private lateinit var organizerPlaybook: OrganizerPlaybook

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } returns "registrationToken"
            coEvery { retrieveTan(any()) } returns "uploadTan"
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        coEvery { submissionServer.submitFakePayload() } just Runs
        coEvery { organizerSubmissionServer.submit(any(), any()) } just Runs

        organizerPlaybook = OrganizerPlaybook(
            appScope = TestCoroutineScope(),
            verificationServer = verificationServer,
            organizerSubmissionServer = organizerSubmissionServer,
            submissionServer = submissionServer,
            dispatcherProvider = TestDispatcherProvider()
        )
    }

    @Test
    fun `submit Upload TAN pass`() = runTest {
        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )
        organizerPlaybook.submit("tan", checkInsReport)

        coVerifySequence {
            // Obtain TAN
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTan(any())
            submissionServer.submitFakePayload()

            // Submission
            verificationServer.retrieveTanFake()
            organizerSubmissionServer.submit(any(), any())
        }
    }

    @Test
    fun `obtainUploadTan - Registration token pass`() = runTest {
        organizerPlaybook.obtainUploadTan(RegistrationRequest(key = "key", type = VerificationKeyType.TELETAN))

        coVerifySequence {
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTan(any())
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `obtainUploadTan - Registration token fail`() = runTest {
        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } throws Exception()
            coEvery { retrieveTan(any()) } returns "uploadTan"
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.obtainUploadTan(RegistrationRequest(key = "key", type = VerificationKeyType.TELETAN))
        }

        coVerifySequence {
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    // /////////////////////////////////////////////////////
    // Registration token Cases
    // ////////////////////////////////////////////////////

    @Test
    fun `submit - registration fails with client error`() = runTest {
        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } throws CwaClientError(400, "CwaClientError")
            coEvery { retrieveTan(any()) } returns "uploadTan"
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.submit("tan", checkInsReport)
        }.errorCode shouldBe OrganizerSubmissionException.ErrorCode.REGTOKEN_OB_CLIENT_ERROR

        coVerifySequence {
            // Obtain TAN
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()

            // Submission
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `submit - registration fails with server error`() = runTest {
        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } throws CwaServerError(500, "CwaServerError")
            coEvery { retrieveTan(any()) } returns "uploadTan"
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.submit("tan", checkInsReport)
        }.errorCode shouldBe OrganizerSubmissionException.ErrorCode.REGTOKEN_OB_SERVER_ERROR

        coVerifySequence {
            // Obtain TAN
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()

            // Submission
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `submit - registration fails with network error`() = runTest {
        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } throws CwaUnknownHostException("CwaUnknownHostException", null)
            coEvery { retrieveTan(any()) } returns "uploadTan"
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.submit("tan", checkInsReport)
        }.errorCode shouldBe OrganizerSubmissionException.ErrorCode.REGTOKEN_OB_NO_NETWORK

        coVerifySequence {
            // Obtain TAN
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()

            // Submission
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `submit - registration fails with other errors`() = runTest {
        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } throws Exception()
            coEvery { retrieveTan(any()) } returns "uploadTan"
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.submit("tan", checkInsReport)
        }.errorCode shouldBe OrganizerSubmissionException.ErrorCode.REGTOKEN_OB_SERVER_ERROR

        coVerifySequence {
            // Obtain TAN
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()

            // Submission
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }
    // /////////////////////////////////////////////////////
    // TAN Cases
    // ////////////////////////////////////////////////////

    @Test
    fun `submit - TAN fails with client error`() = runTest {
        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } returns "RegistrationToken"
            coEvery { retrieveTan(any()) } throws CwaClientError(400, "CwaClientError")
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.submit("tan", checkInsReport)
        }.errorCode shouldBe OrganizerSubmissionException.ErrorCode.TAN_OB_CLIENT_ERROR

        coVerifySequence {
            // Obtain TAN
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTan(any())
            submissionServer.submitFakePayload()

            // Submission
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `submit - TAN fails with server error`() = runTest {
        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } returns "RegistrationToken"
            coEvery { retrieveTan(any()) } throws CwaServerError(500, "CwaServerError")
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.submit("tan", checkInsReport)
        }.errorCode shouldBe OrganizerSubmissionException.ErrorCode.TAN_OB_SERVER_ERROR

        coVerifySequence {
            // Obtain TAN
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTan(any())
            submissionServer.submitFakePayload()

            // Submission
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `submit - TAN fails with network error`() = runTest {
        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } returns "RegistrationToken"
            coEvery { retrieveTan(any()) } throws CwaUnknownHostException("CwaUnknownHostException", null)
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.submit("tan", checkInsReport)
        }.errorCode shouldBe OrganizerSubmissionException.ErrorCode.TAN_OB_NO_NETWORK

        coVerifySequence {
            // Obtain TAN
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTan(any())
            submissionServer.submitFakePayload()

            // Submission
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `submit - TAN fails with other errors`() = runTest {
        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } returns "RegistrationToken"
            coEvery { retrieveTan(any()) } throws Exception()
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.submit("tan", checkInsReport)
        }.errorCode shouldBe OrganizerSubmissionException.ErrorCode.TAN_OB_SERVER_ERROR

        coVerifySequence {
            // Obtain TAN
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTan(any())
            submissionServer.submitFakePayload()

            // Submission
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    // /////////////////////////////////////////////////////
    // Submission Cases
    // ////////////////////////////////////////////////////

    @Test
    fun `submit fails with client error`() = runTest {
        coEvery { organizerSubmissionServer.submit(any(), any()) } throws
            CwaClientError(400, "CwaClientError")
        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.submit("tan", checkInsReport)
        }.errorCode shouldBe OrganizerSubmissionException.ErrorCode.SUBMISSION_OB_CLIENT_ERROR

        coVerifySequence {
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTan(any())
            submissionServer.submitFakePayload()
            verificationServer.retrieveTanFake()
            organizerSubmissionServer.submit(any(), any())
        }
    }

    @Test
    fun `submit fails with server error`() = runTest {
        coEvery { organizerSubmissionServer.submit(any(), any()) } throws
            CwaServerError(500, "CwaServerError")

        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.submit("tan", checkInsReport)
        }.errorCode shouldBe OrganizerSubmissionException.ErrorCode.SUBMISSION_OB_SERVER_ERROR

        coVerifySequence {
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTan(any())
            submissionServer.submitFakePayload()
            verificationServer.retrieveTanFake()
            organizerSubmissionServer.submit(any(), any())
        }
    }

    @Test
    fun `submit fails with network error`() = runTest {
        coEvery { organizerSubmissionServer.submit(any(), any()) } throws
            CwaUnknownHostException("CwaUnknownHostException", null)

        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.submit("tan", checkInsReport)
        }.errorCode shouldBe OrganizerSubmissionException.ErrorCode.SUBMISSION_OB_NO_NETWORK

        coVerifySequence {
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTan(any())
            submissionServer.submitFakePayload()
            verificationServer.retrieveTanFake()
            organizerSubmissionServer.submit(any(), any())
        }
    }

    @Test
    fun `submit fails with other errors`() = runTest {
        coEvery { organizerSubmissionServer.submit(any(), any()) } throws Exception()

        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<OrganizerSubmissionException> {
            organizerPlaybook.submit("tan", checkInsReport)
        }.errorCode shouldBe OrganizerSubmissionException.ErrorCode.SUBMISSION_OB_SERVER_ERROR

        coVerifySequence {
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTan(any())
            submissionServer.submitFakePayload()
            verificationServer.retrieveTanFake()
            organizerSubmissionServer.submit(any(), any())
        }
    }
}
